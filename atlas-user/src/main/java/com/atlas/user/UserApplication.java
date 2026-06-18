package com.atlas.user;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * Atlas 用户权限管理微服务
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.atlas.common", "com.atlas.user"})
@MapperScan("com.atlas.user.mapper")
public class UserApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}