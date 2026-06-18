package com.atlas.open.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.open.entity.ApiEndpointConfig;
import com.atlas.open.entity.ApiIntegrationLog;
import com.atlas.open.entity.ThirdPartyApiConfig;
import com.atlas.open.service.DynamicApiService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * API 配置管理 Controller — CRUD 管理第三方 API 配置和接口定义 / API configuration management Controller — CRUD for third-party API configs and endpoint definitions
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@RestController
@RequestMapping("/api/open/config")
@RequiredArgsConstructor
public class ApiConfigController {

    private final DynamicApiService dynamicApiService;

    // ==================== 配置管理 / Config Management ====================

    @GetMapping("/page")
    @RequirePermission("open:config:view")
    public Result<Page<ThirdPartyApiConfig>> configPage(@RequestParam(defaultValue = "1") int page,
                                                         @RequestParam(defaultValue = "10") int size) {
        return Result.ok(dynamicApiService.configPage(page, size));
    }

    @GetMapping("/{id}")
    @RequirePermission("open:config:view")
    public Result<ThirdPartyApiConfig> getConfig(@PathVariable Long id) {
        return Result.ok(dynamicApiService.getConfig(id));
    }

    @GetMapping("/{configId}/endpoints")
    @RequirePermission("open:config:view")
    public Result<List<ApiEndpointConfig>> listEndpoints(@PathVariable Long configId) {
        return Result.ok(dynamicApiService.listEndpoints(configId));
    }

    // ==================== 调用日志 / Call Logs ====================

    @GetMapping("/log/page")
    @RequirePermission("open:config:view")
    public Result<Page<ApiIntegrationLog>> logPage(@RequestParam(required = false) Long configId,
                                                    @RequestParam(defaultValue = "1") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return Result.ok(dynamicApiService.logPage(configId, page, size));
    }

    // ==================== 连通性测试 / Connectivity Test ====================

    /**
     * 测试 API 连通性 / Test API connectivity
     */
    @PostMapping("/test")
    @RequirePermission("open:config:test")
    public Result<Object> testConnection(@RequestParam String endpointCode,
                                         @RequestParam(required = false) java.util.Map<String, Object> params) {
        try {
            Object result = dynamicApiService.execute(endpointCode,
                    params != null ? params : java.util.Collections.emptyMap());
            return Result.ok(result);
        } catch (Exception e) {
            return Result.fail(500, "连通性测试失败: " + e.getMessage());
        }
    }
}
