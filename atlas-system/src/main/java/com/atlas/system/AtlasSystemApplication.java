package com.atlas.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Atlas System 启动类 — 系统管理微服务（权限/角色/用户管理） /
 * Atlas System Application — system management microservice (permission/role/user admin)
 * <p>
 * 端口 8092，独立部署，负责权限配置的集中管理 /
 * Port 8092, independently deployed, centralized permission configuration management
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@SpringBootApplication(scanBasePackages = {"com.atlas.system", "com.atlas.common"})
public class AtlasSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(AtlasSystemApplication.class, args);
    }
}
