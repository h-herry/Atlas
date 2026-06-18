package com.atlas.common.i18n.config;

import com.atlas.common.i18n.service.I18nService;
import org.springframework.context.support.AbstractMessageSource;

import java.text.MessageFormat;
import java.util.Locale;

/**
 * Spring MessageSource 桥接 — 将 I18nService 适配到 Spring 的 MessageSource 体系
 *
 * <p>兼容 @Valid message、Thymeleaf #messages 等 Spring 内置国际化场景</p>
 */
public class I18nMessageSource extends AbstractMessageSource {

    private final I18nService i18nService;

    public I18nMessageSource(I18nService i18nService) {
        this.i18nService = i18nService;
    }

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String message = i18nService.translate(code, locale);
        return new MessageFormat(message, locale);
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        return i18nService.translate(code, locale);
    }
}
