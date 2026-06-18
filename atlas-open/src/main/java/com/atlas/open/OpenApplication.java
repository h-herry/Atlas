package com.atlas.open;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Atlas Open Platform — 可配置 API 对接模块 / Atlas Open Platform — configurable API integration module
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@SpringBootApplication
@EnableDiscoveryClient
public class OpenApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenApplication.class, args);
    }
}
