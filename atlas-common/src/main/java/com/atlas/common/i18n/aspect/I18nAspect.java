package com.atlas.common.i18n.aspect;

import com.atlas.common.i18n.annotation.I18nField;
import com.atlas.common.i18n.annotation.I18nTranslate;
import com.atlas.common.i18n.service.I18nService;
import com.atlas.common.web.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Field;
import java.util.*;

/**
 * I18n 翻译切面 — 拦截 @I18nTranslate 注解方法
 *
 * <p>流程：执行原方法 → 获取返回的 Result → 递归遍历 data → 翻译 @I18nField 字段</p>
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class I18nAspect {

    private final I18nService i18nService;

    /**
     * 拦截所有带 @I18nTranslate 的 Controller 方法
     */
    @Around("@annotation(com.atlas.common.i18n.annotation.I18nTranslate)")
    public Object translateResult(ProceedingJoinPoint joinPoint) throws Throwable {
        // 1. 执行原方法
        Object result = joinPoint.proceed();

        // 2. 只处理返回 Result 的场景
        if (!(result instanceof Result)) {
            return result;
        }

        // 3. 解析注解的 fields 参数
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        I18nTranslate annotation = signature.getMethod().getAnnotation(I18nTranslate.class);
        String[] specifiedFields = annotation.fields();

        // 4. 获取当前请求 Locale
        Locale locale = resolveLocale();

        // 5. 递归翻译 Result.data
        Result<?> res = (Result<?>) result;
        if (res.getData() != null) {
            translateObject(res.getData(), locale, new HashSet<>(Arrays.asList(specifiedFields)));
        }

        return res;
    }

    // ==================== 递归翻译引擎 ====================

    @SuppressWarnings("unchecked")
    private void translateObject(Object obj, Locale locale, Set<String> specifiedFields) {
        if (obj == null) {
            return;
        }

        // Collection
        if (obj instanceof Collection) {
            for (Object item : (Collection<?>) obj) {
                translateObject(item, locale, specifiedFields);
            }
            return;
        }

        // Map
        if (obj instanceof Map) {
            // Map 不翻译（字段名不可控）
            return;
        }

        // 基本类型/包装类型/String → 停止递归
        if (isPrimitiveOrWrapper(obj.getClass())) {
            return;
        }

        // POJO: 遍历 Field，翻译 @I18nField 标注的 String 字段
        Field[] fields = obj.getClass().getDeclaredFields();
        for (Field field : fields) {
            field.setAccessible(true);

            // 判断是否需要翻译：明确指定了 fields 则用指定列表，否则按 @I18nField 注解
            boolean shouldTranslate;
            if (!specifiedFields.isEmpty()) {
                shouldTranslate = specifiedFields.contains(field.getName());
            } else {
                shouldTranslate = field.isAnnotationPresent(I18nField.class);
            }

            if (!shouldTranslate || !field.getType().equals(String.class)) {
                continue;
            }

            try {
                String originalValue = (String) field.get(obj);
                if (originalValue != null && !originalValue.isEmpty()) {
                    String translated = i18nService.translate(originalValue, locale);
                    field.set(obj, translated);
                }
            } catch (IllegalAccessException e) {
                log.debug("无法翻译字段 {}: {}", field.getName(), e.getMessage());
            }
        }

        // 递归处理嵌套对象字段（非基本类型的引用字段）
        for (Field field : fields) {
            field.setAccessible(true);
            Class<?> fieldType = field.getType();

            if (isPrimitiveOrWrapper(fieldType) || fieldType == String.class) {
                continue;
            }

            try {
                Object nestedValue = field.get(obj);
                if (nestedValue != null) {
                    translateObject(nestedValue, locale, specifiedFields);
                }
            } catch (IllegalAccessException e) {
                log.debug("无法递归翻译嵌套字段 {}: {}", field.getName(), e.getMessage());
            }
        }
    }

    private boolean isPrimitiveOrWrapper(Class<?> clazz) {
        return clazz.isPrimitive()
                || clazz == Boolean.class || clazz == Byte.class
                || clazz == Character.class || clazz == Short.class
                || clazz == Integer.class || clazz == Long.class
                || clazz == Float.class || clazz == Double.class
                || Number.class.isAssignableFrom(clazz)
                || clazz == String.class
                || clazz.isEnum();
    }

    /**
     * 从 Accept-Language 请求头解析 Locale
     */
    private Locale resolveLocale() {
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                return request.getLocale();
            }
        } catch (Exception e) {
            log.debug("无法解析请求 Locale，使用默认: zh-CN");
        }
        return Locale.SIMPLIFIED_CHINESE;
    }
}
