package com.atlas.user.system;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * System 配置类 — RBAC 权限管理（已合并入 user 模块） /
 * System configuration — RBAC permission management (merged into user module)
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Configuration
@ComponentScan(basePackages = {"com.atlas.user.system", "com.atlas.common"})
public class AtlasSystemConfiguration {
}
