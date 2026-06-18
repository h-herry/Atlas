package com.atlas.receipt;

import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Atlas 收货管理微服务 / Atlas Receipt Management Microservice
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableAutoDataSourceProxy
@EnableScheduling
@ComponentScan(basePackages = {"com.atlas.common", "com.atlas.receipt"})
@MapperScan({"com.atlas.receipt.mapper", "com.atlas.receipt.delivery.mapper"})
public class ReceiptApplication {

    public static void main(String[] args) {
        SpringApplication.run(ReceiptApplication.class, args);
    }
}
