package com.atlas.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Atlas API 网关 / Atlas API Gateway
 * <p>
 * 基于 Spring Cloud Gateway + Nacos 服务发现，统一对外暴露 REST API，
 * 实现路由转发、负载均衡、跨域处理。 /
 * Based on Spring Cloud Gateway + Nacos service discovery, exposes unified REST APIs
 * with routing, load balancing, and CORS handling.
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
