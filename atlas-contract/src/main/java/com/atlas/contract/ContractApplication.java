package com.atlas.contract;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Atlas 合同管理微服务 / Atlas Contract Management Microservice
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.atlas.common", "com.atlas.contract"})
@MapperScan("com.atlas.contract.mapper")
public class ContractApplication {
    public static void main(String[] args) {
        SpringApplication.run(ContractApplication.class, args);
    }
}
