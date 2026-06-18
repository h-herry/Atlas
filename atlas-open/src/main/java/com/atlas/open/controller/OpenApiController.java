package com.atlas.open.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.open.entity.ApiCallLog;
import com.atlas.open.entity.OpenApiClient;
import com.atlas.open.entity.WebhookSubscription;
import com.atlas.open.service.OpenApiService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 开放平台管理 Controller / Open Platform Management Controller
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@RestController
@RequestMapping("/api/open")
@RequiredArgsConstructor
@Tag(name = "开放API / Open API")
public class OpenApiController {

    private final OpenApiService openApiService;

    // ==================== 客户端管理 / Client Management ====================

    @PostMapping("/client/register")
    @RequirePermission("open:add")
    public Result<OpenApiClient> register(@RequestParam String clientName,
                                           @RequestParam String accessApis,
                                           @RequestParam(defaultValue = "100") Integer rateLimitPerMin,
                                           @RequestParam(required = false) LocalDate expireDate) {
        return Result.ok(openApiService.register(clientName, accessApis, rateLimitPerMin, expireDate));
    }

    @GetMapping("/client/page")
    @RequirePermission("open:view")
    public Result<Page<OpenApiClient>> page(@RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(openApiService.page(page, size));
    }

    @PutMapping("/client/{id}/disable")
    @RequirePermission("open:edit")
    public Result<Void> disableClient(@PathVariable Long id) {
        openApiService.disableClient(id);
        return Result.ok();
    }

    // ==================== API 调用日志 / API Call Logs ====================

    @GetMapping("/log/page")
    @RequirePermission("open:view")
    public Result<Page<ApiCallLog>> logPage(@RequestParam(required = false) Long clientId,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(openApiService.logPage(clientId, page, size));
    }

    // ==================== Webhook 管理 / Webhook Management ====================

    @PostMapping("/webhook/subscribe")
    @RequirePermission("open:add")
    public Result<Void> subscribe(@RequestParam Long clientId,
                                   @RequestParam String eventType,
                                   @RequestParam String callbackUrl,
                                   @RequestParam(required = false) String secret) {
        openApiService.subscribe(clientId, eventType, callbackUrl, secret);
        return Result.ok();
    }

    @GetMapping("/webhook/{clientId}")
    @RequirePermission("open:view")
    public Result<List<WebhookSubscription>> listSubscriptions(@PathVariable Long clientId) {
        return Result.ok(openApiService.listSubscriptions(clientId));
    }

    @PutMapping("/webhook/{id}/unsubscribe")
    @RequirePermission("open:edit")
    public Result<Void> unsubscribe(@PathVariable Long id) {
        openApiService.unsubscribe(id);
        return Result.ok();
    }
}
