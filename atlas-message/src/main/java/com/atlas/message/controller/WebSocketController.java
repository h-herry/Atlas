package com.atlas.message.controller;

import com.atlas.common.web.Result;
import com.atlas.message.dto.PushRequest;
import com.atlas.message.service.MessagePushService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * WebSocket 消息推送 Controller — 供内部微服务通过 Feign 调用 /
 * WebSocket message push Controller — for internal microservices to call via Feign
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Tag(name = "消息推送 / Message Push", description = "内部推送接口 — 供业务微服务 Feign 调用")
@RestController
@RequestMapping("/api/message")
@RequiredArgsConstructor
public class WebSocketController {

    private final MessagePushService messagePushService;

    /**
     * 向指定供应商推送消息 / Push message to specific supplier
     */
    @Operation(summary = "向供应商推送消息 / Push message to supplier")
    @PostMapping("/push/supplier")
    public Result<Map<String, Object>> pushToSupplier(@RequestBody PushRequest request) {
        messagePushService.pushToSupplier(request);
        return Result.ok(Map.of("status", "ok", "method", "pushToSupplier"));
    }

    /**
     * 向全部供应商广播 / Broadcast to all suppliers
     */
    @Operation(summary = "向全部供应商广播 / Broadcast to all suppliers")
    @PostMapping("/push/broadcast")
    public Result<Map<String, Object>> broadcastToAll(@RequestBody PushRequest request) {
        messagePushService.broadcastToAllSuppliers(request);
        return Result.ok(Map.of("status", "ok", "method", "broadcast"));
    }

    /**
     * 向内部用户推送 / Push to internal user
     */
    @Operation(summary = "向内部用户推送 / Push to internal user")
    @PostMapping("/push/user")
    public Result<Map<String, Object>> pushToUser(@RequestBody PushRequest request) {
        messagePushService.pushToUser(request);
        return Result.ok(Map.of("status", "ok", "method", "pushToUser"));
    }
}
