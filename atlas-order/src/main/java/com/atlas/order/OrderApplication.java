package com.atlas.order;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Atlas 订单管理微服务 / Atlas Order Management Microservice
 * <p>
 * 提供 JIT 交付管理、多工厂分单等制造业场景核心能力。 /
 * Provides JIT delivery management, multi-plant order allocation for manufacturing scenarios.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@ComponentScan(basePackages = {"com.atlas.common", "com.atlas.order"})
@MapperScan("com.atlas.order.mapper")
public class OrderApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class, args);
    }
}
