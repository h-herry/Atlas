package com.atlas.delivery;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Atlas 交付管理微服务 / Atlas Delivery Management Microservice
 * <p>
 * 提供 VMI 库存监控、补货通知等供应商协同交付能力。 /
 * Provides VMI inventory monitoring, replenishment notification for supplier delivery collaboration.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@ComponentScan(basePackages = {"com.atlas.common", "com.atlas.delivery"})
@MapperScan("com.atlas.delivery.mapper")
public class DeliveryApplication {

    public static void main(String[] args) {
        SpringApplication.run(DeliveryApplication.class, args);
    }
}
