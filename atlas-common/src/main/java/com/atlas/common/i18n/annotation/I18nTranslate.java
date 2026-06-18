package com.atlas.common.i18n.annotation;

import java.lang.annotation.*;

/**
 * 标记 Controller 方法，对其返回的 Result.data 自动执行 i18n 翻译
 *
 * <pre>
 * // 翻译 data 中所有带 @I18nField 注解的字段
 * &#64;I18nTranslate(fields = {"statusText"})
 * public Result&lt;OrderVO&gt; getOrder(Long id) { ... }
 * </pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
public @interface I18nTranslate {

    /**
     * 指定需要翻译的字段名列表。为空时自动扫描 @I18nField 标注的字段
     */
    String[] fields() default {};
}
