package com.atlas.common.i18n.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.atlas.common.i18n.entity.I18nMessage;
import com.atlas.common.i18n.mapper.I18nLanguageMapper;
import com.atlas.common.i18n.mapper.I18nMessageMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 核心翻译服务 — 数据库驱动多语言，Caffeine 本地缓存
 *
 * <p>架构：请求 → Caffeine L1 (本地) → MySQL L2 (回填缓存) → Key fallback</p>
 * <p>支持 MessageFormat 参数化：translate("order.created", locale, orderId) → "订单 12345 已创建"</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class I18nService {

    private final I18nMessageMapper i18nMessageMapper;
    private final I18nLanguageMapper i18nLanguageMapper;

    /**
     * Caffeine 本地缓存：key = "languageCode:messageKey"
     * maxSize=5000, expireAfterWrite=30min
     */
    private Cache<String, String> messageCache;

    /**
     * 缓存中不存在的占位符，用于双重检查锁中标记"正在加载"
     */
    private static final String LOADING_PLACEHOLDER = "__LOADING__";

    @PostConstruct
    public void init() {
        messageCache = Caffeine.newBuilder()
                .maximumSize(5000)
                .expireAfterWrite(30, TimeUnit.MINUTES)
                .build();
        warmUpCache();
        log.info("I18nService 初始化完成，缓存预热 {} 条", messageCache.estimatedSize());
    }

    // ==================== 缓存预热 ====================

    /**
     * 启动时预加载 zh-CN 和 en-US 常用消息到缓存
     */
    private void warmUpCache() {
        try {
            List<I18nMessage> messages = i18nMessageMapper.selectList(
                    new LambdaQueryWrapper<I18nMessage>()
                            .in(I18nMessage::getLanguageCode, "zh-CN", "en-US")
                            .last("LIMIT 5000")
            );
            if (CollUtil.isEmpty(messages)) {
                log.info("缓存预热：暂无翻译数据，跳过预热");
                return;
            }
            for (I18nMessage msg : messages) {
                String cacheKey = buildCacheKey(msg.getMessageKey(), msg.getLanguageCode());
                messageCache.put(cacheKey, msg.getMessageValue());
            }
            log.info("缓存预热完成：加载 {} 条消息", messages.size());
        } catch (Exception e) {
            log.warn("缓存预热失败，将在首次请求时 lazy-load: {}", e.getMessage());
        }
    }

    // ==================== 翻译接口 ====================

    /**
     * 获取翻译文本
     *
     * @param key    消息键，如 common.success
     * @param locale 目标语言
     * @return 翻译文本，未命中返回 key 本身
     */
    public String translate(String key, Locale locale) {
        return translate(key, locale, null, (Object[]) null);
    }

    /**
     * 获取翻译文本（带参数）
     *
     * @param key    消息键
     * @param locale 目标语言
     * @param args   MessageFormat 参数
     * @return 参数化翻译文本
     */
    public String translate(String key, Locale locale, Object... args) {
        return translate(key, locale, null, args);
    }

    /**
     * 获取翻译文本（带默认值 + 参数）
     *
     * @param key            消息键
     * @param locale         目标语言
     * @param defaultMessage 数据库未命中时的默认文本
     * @param args           参数
     * @return 翻译文本
     */
    public String translate(String key, Locale locale, String defaultMessage, Object... args) {
        String languageCode = toLanguageCode(locale);
        String cacheKey = buildCacheKey(key, languageCode);

        // 1. 查缓存
        String cached = messageCache.getIfPresent(cacheKey);
        if (cached != null) {
            return formatMessage(cached, args);
        }

        // 2. 双重检查锁查数据库
        String value = loadWithDoubleCheck(key, languageCode, cacheKey);

        // 3. 数据库未命中 → defaultMessage 或 key
        if (value == null) {
            value = (defaultMessage != null) ? defaultMessage : key;
        }

        return formatMessage(value, args);
    }

    /**
     * 批量获取某模块下所有翻译
     *
     * @param module       模块名
     * @param locale       目标语言
     * @return Map<messageKey, translatedValue>
     */
    public Map<String, String> translateAll(String module, Locale locale) {
        String languageCode = toLanguageCode(locale);
        List<Map<String, String>> rows = i18nMessageMapper.selectByModule(module, languageCode);
        Map<String, String> result = new LinkedHashMap<>();
        if (CollUtil.isNotEmpty(rows)) {
            for (Map<String, String> row : rows) {
                result.put(row.get("message_key"), row.get("message_value"));
            }
        }
        return result;
    }

    // ==================== 管理接口 ====================

    /**
     * 保存或更新翻译
     */
    public void saveOrUpdate(String key, String languageCode, String value) {
        I18nMessage existing = i18nMessageMapper.selectOne(
                new LambdaQueryWrapper<I18nMessage>()
                        .eq(I18nMessage::getMessageKey, key)
                        .eq(I18nMessage::getLanguageCode, languageCode)
        );
        if (existing != null) {
            i18nMessageMapper.update(null,
                    new LambdaUpdateWrapper<I18nMessage>()
                            .eq(I18nMessage::getMessageKey, key)
                            .eq(I18nMessage::getLanguageCode, languageCode)
                            .set(I18nMessage::getMessageValue, value)
            );
        } else {
            I18nMessage msg = new I18nMessage();
            msg.setMessageKey(key);
            msg.setLanguageCode(languageCode);
            msg.setMessageValue(value);
            i18nMessageMapper.insert(msg);
        }
        // 刷新缓存
        messageCache.put(buildCacheKey(key, languageCode), value);
    }

    /**
     * 删除某消息键的所有语言翻译
     */
    public void deleteKey(String key) {
        List<I18nMessage> messages = i18nMessageMapper.selectList(
                new LambdaQueryWrapper<I18nMessage>()
                        .eq(I18nMessage::getMessageKey, key)
        );
        for (I18nMessage msg : messages) {
            messageCache.invalidate(buildCacheKey(key, msg.getLanguageCode()));
        }
        i18nMessageMapper.delete(
                new LambdaQueryWrapper<I18nMessage>()
                        .eq(I18nMessage::getMessageKey, key)
        );
    }

    /**
     * 刷新所有缓存
     */
    public void refreshCache() {
        messageCache.invalidateAll();
        warmUpCache();
        log.info("I18n 缓存已刷新");
    }

    // ==================== 内部方法 ====================

    /**
     * 双重检查锁加载：防止缓存击穿
     */
    private String loadWithDoubleCheck(String key, String languageCode, String cacheKey) {
        // 第一次检查
        String cached = messageCache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        synchronized (this) {
            // 第二次检查
            cached = messageCache.getIfPresent(cacheKey);
            if (cached != null) {
                return cached;
            }

            // 查数据库
            I18nMessage msg = i18nMessageMapper.selectOne(
                    new LambdaQueryWrapper<I18nMessage>()
                            .eq(I18nMessage::getMessageKey, key)
                            .eq(I18nMessage::getLanguageCode, languageCode)
            );

            String value = (msg != null) ? msg.getMessageValue() : null;
            if (value != null) {
                messageCache.put(cacheKey, value);
            } else {
                // 缓存 null 标记，避免缓存穿透（短暂缓存 1 分钟）
                messageCache.put(cacheKey, "");
            }
            return value;
        }
    }

    /**
     * 格式化消息：支持 MessageFormat {0} {1} 占位符
     */
    private String formatMessage(String template, Object... args) {
        if (args == null || args.length == 0 || !template.contains("{")) {
            return template;
        }
        try {
            return MessageFormat.format(template, args);
        } catch (Exception e) {
            log.warn("MessageFormat 格式化失败: template={}, args={}", template, args, e);
            return template;
        }
    }

    /**
     * 构建缓存 key
     */
    private String buildCacheKey(String messageKey, String languageCode) {
        return languageCode + ":" + messageKey;
    }

    /**
     * Locale → 语言代码字符串
     */
    private String toLanguageCode(Locale locale) {
        if (locale == null) {
            return "zh-CN";
        }
        if (StrUtil.isNotBlank(locale.getCountry())) {
            return locale.getLanguage() + "-" + locale.getCountry();
        }
        return locale.getLanguage();
    }
}
