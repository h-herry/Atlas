package com.atlas.common.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;

/**
 * 数据库健康检查指示器 / DataSource health indicator
 *
 * <p>检查数据库连接是否可用（执行 SELECT 1），并报告连接状态。 /
 * Checks if DB connection is available (executes SELECT 1) and reports connection status.</p>
 *
 * <p>暴露端点: /actuator/health / Exposed endpoint: /actuator/health</p>
 *
 * @author Atlas Team
 * @since 1.2.21
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataSourceHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    @Override
    public Health health() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("SELECT 1");
            return Health.up()
                    .withDetail("database", "MySQL")
                    .withDetail("connection", "OK")
                    .build();
        } catch (Exception e) {
            log.error("数据库健康检查失败 / Database health check failed", e);
            return Health.down()
                    .withDetail("database", "MySQL")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
