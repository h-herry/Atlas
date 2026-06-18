package com.atlas.inventory;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Atlas 库存管理微服务
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.atlas.common", "com.atlas.inventory"})
@MapperScan("com.atlas.inventory.mapper")
public class InventoryApplication {

    public static void main(String[] args) {
        SpringApplication.run(InventoryApplication.class, args);
    }
}
