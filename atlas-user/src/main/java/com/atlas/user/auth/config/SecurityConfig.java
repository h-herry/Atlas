package com.atlas.user.auth.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * atlas-user 模块 Spring Security 配置 — 补充放行 auth 子模块端点 /
 * atlas-user module Security config — supplements auth sub-module endpoint permit
 * <p>
 * 与 atlas-common 的 SecurityConfig 共存：本配置 @Order(0) 优先处理 /auth/**，
 * 其余请求由 common 的默认 SecurityFilterChain 处理。
 * Coexists with atlas-common SecurityConfig: this @Order(0) handles /auth/** first,
 * remaining requests fall through to common's default SecurityFilterChain.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * 高优先级过滤器链：放行 /auth/** 及内部调用端点 /
     * High-priority filter chain: permits /auth/** and internal endpoints
     */
    @Bean
    @Order(0)
    @ConditionalOnMissingBean(name = "authSecurityFilterChain")
    public SecurityFilterChain authSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher("/auth/**", "/token/validate")
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/logout", "/auth/refresh", "/auth/check").permitAll()
                .requestMatchers("/token/validate").permitAll()
                .anyRequest().authenticated()
            );

        return http.build();
    }
}
