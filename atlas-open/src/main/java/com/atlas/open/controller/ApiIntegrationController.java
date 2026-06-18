package com.atlas.open.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.open.service.DynamicApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * API 集成调用入口 — 动态执行已配置的第三方 API 调用 / API integration entry — dynamically executes configured third-party API calls
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@RestController
@RequestMapping("/api/open/integration")
@RequiredArgsConstructor
public class ApiIntegrationController {

    private final DynamicApiService dynamicApiService;

    /**
     * 动态执行 API 调用 / Dynamically execute API call
     *
     * @param endpointCode 接口编码 / @param endpointCode 接口编码 / Endpoint code
     * @param params       请求参数（变量替换值） / @param params       请求参数（变量替换值） / Request parameters (variable substitution values)
     * @return API 响应映射后的结果 / @return API 响应映射后的结果 / Mapped API response result
     */
    @GetMapping("/{endpointCode}")
    @RequirePermission("open:integration:execute")
    public Result<Object> execute(@PathVariable String endpointCode,
                                  @RequestParam Map<String, Object> params) {
        return Result.ok(dynamicApiService.execute(endpointCode, params));
    }
}
