package com.atlas.supplier;

import io.seata.spring.annotation.datasource.EnableAutoDataSourceProxy;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Atlas 供应商管理微服务 / Atlas supplier management microservice
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atlas.supplier.feign")
@EnableAsync
@EnableAutoDataSourceProxy
@ComponentScan(basePackages = {"com.atlas.common", "com.atlas.supplier"})
@MapperScan("com.atlas.supplier.mapper")
public class SupplierApplication {
    public static void main(String[] args) {
        SpringApplication.run(SupplierApplication.class, args);
    }
}
