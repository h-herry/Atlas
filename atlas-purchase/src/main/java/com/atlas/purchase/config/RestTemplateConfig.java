package com.atlas.purchase.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * RestTemplate 配置 / RestTemplate configuration
 * <p>
 * 创建支持 Nacos 服务发现的 LoadBalanced RestTemplate Bean，
 * 用于通过服务名调用其他微服务（如 atlas-inventory）。 /
 * Creates a LoadBalanced RestTemplate with Nacos service discovery support,
 * used for calling other microservices by service name (e.g. atlas-inventory).
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    @LoadBalanced
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
