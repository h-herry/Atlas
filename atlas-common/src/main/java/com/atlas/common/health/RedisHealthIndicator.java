package com.atlas.common.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

/**
 * Redis 健康检查指示器 / Redis health indicator
 *
 * <p>检查 Redis 连接是否可用（执行 PING），并报告连接状态。 /
 * Checks if Redis connection is available (executes PING) and reports connection status.</p>
 *
 * <p>使用标准 RedisConnectionFactory（阻塞式），适用于所有服务模块。 /
 * Uses standard RedisConnectionFactory (blocking), applicable to all service modules.</p>
 *
 * <p>暴露端点: /actuator/health / Exposed endpoint: /actuator/health</p>
 *
 * @author Atlas Team
 * @since 1.2.21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisConnectionFactory connectionFactory;

    @Override
    public Health health() {
        try (RedisConnection conn = connectionFactory.getConnection()) {
            String result = conn.ping();
            if ("PONG".equals(result)) {
                return Health.up()
                        .withDetail("redis", "PONG")
                        .withDetail("connection", "OK")
                        .build();
            } else {
                return Health.down()
                        .withDetail("redis", result)
                        .withDetail("error", "Unexpected PING response")
                        .build();
            }
        } catch (Exception e) {
            log.error("Redis 健康检查失败 / Redis health check failed", e);
            return Health.down()
                    .withDetail("redis", "DOWN")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
