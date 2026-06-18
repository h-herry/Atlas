package com.atlas.supplier.feign;

import com.atlas.common.web.Result;
import com.atlas.supplier.feign.fallback.MessageFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

/**
 * 消息推送 Feign 客户端 — 调用 atlas-message 微服务发送实时消息 /
 * Message push Feign client — calls atlas-message microservice to send real-time messages
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@FeignClient(
        name = "atlas-message",
        path = "/api/message",
        fallback = MessageFeignFallback.class
)
public interface MessageFeignClient {

    /**
     * 向指定供应商推送消息 / Push message to specific supplier
     *
     * @param request 推送请求（包含 supplierId/title/content/type 等）/ Push request
     * @return 操作结果 / Result
     */
    @PostMapping("/push/supplier")
    Result<Map<String, Object>> pushToSupplier(@RequestBody Map<String, Object> request);

    /**
     * 向全部供应商广播 / Broadcast to all suppliers
     *
     * @param request 推送请求 / Push request
     * @return 操作结果 / Result
     */
    @PostMapping("/push/broadcast")
    Result<Map<String, Object>> broadcastToAll(@RequestBody Map<String, Object> request);

    /**
     * 向内部用户推送 / Push to internal user
     *
     * @param request 推送请求 / Push request
     * @return 操作结果 / Result
     */
    @PostMapping("/push/user")
    Result<Map<String, Object>> pushToUser(@RequestBody Map<String, Object> request);
}
