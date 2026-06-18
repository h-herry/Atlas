package com.atlas.material;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Atlas 物料管理微服务 / Atlas Material Management Microservice
 * <p>
 * 提供物料主数据管理及批次管理能力。 / Provides material master data and lot management capabilities.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.atlas.common", "com.atlas.material"})
@MapperScan("com.atlas.material.mapper")
public class MaterialApplication {

    public static void main(String[] args) {
        SpringApplication.run(MaterialApplication.class, args);
    }
}
