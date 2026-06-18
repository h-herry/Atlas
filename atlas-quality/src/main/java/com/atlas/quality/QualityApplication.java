package com.atlas.quality;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Atlas 质量管理微服务 / Atlas Quality Management Microservice
 * <p>
 * 提供 PPAP 提交跟踪、来料批次追溯等汽车行业质量管理能力。 /
 * Provides PPAP submission tracking, incoming lot traceability for automotive quality management.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.atlas.common", "com.atlas.quality"})
@MapperScan("com.atlas.quality.mapper")
public class QualityApplication {

    public static void main(String[] args) {
        SpringApplication.run(QualityApplication.class, args);
    }
}
