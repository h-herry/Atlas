package com.atlas.open.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

/**
 * API 对接全局配置（支持 Nacos 热刷新） / API integration global config (supports Nacos hot refresh)
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Component
@RefreshScope
@ConfigurationProperties(prefix = "atlas.open")
public class ApiConfigProperties {

    /** 默认超时（毫秒） / Default timeout (ms) */
    private Integer defaultTimeoutMs = 10000;

    /** 默认重试次数 / Default retry count */
    private Integer defaultRetryCount = 3;

    /** HMAC 时间戳允许偏差（秒） / HMAC timestamp skew tolerance (seconds) */
    private Integer timestampSkewSeconds = 300;

    /** OAuth2 Token 缓存 TTL（秒） / OAuth2 token cache TTL (seconds) */
    private Integer tokenCacheTtlSeconds = 1800;
}
