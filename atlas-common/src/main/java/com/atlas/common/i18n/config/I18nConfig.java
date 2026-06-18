package com.atlas.common.i18n.config;

import com.atlas.common.i18n.service.I18nService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import java.util.Locale;

/**
 * I18n 配置 — 注册 LocaleResolver + I18nMessageSource
 *
 * <p>LocaleResolver 从 Accept-Language 请求头解析语言，默认 zh-CN</p>
 */
@Configuration
@RequiredArgsConstructor
public class I18nConfig implements WebMvcConfigurer {

    private final I18nService i18nService;

    /**
     * 基于 Accept-Language 请求头的语言解析器
     */
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.SIMPLIFIED_CHINESE);
        return resolver;
    }

    /**
     * 自定义 MessageSource，桥接 Spring MessageSource 接口 → I18nService
     */
    @Bean
    public I18nMessageSource i18nMessageSource() {
        return new I18nMessageSource(i18nService);
    }
}
