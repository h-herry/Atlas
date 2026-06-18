package com.atlas.common.message.config;

import com.atlas.common.websocket.MessageTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Redis Pub/Sub 消息订阅者 — 接收跨实例广播，转发到本地 WebSocket /
 * Redis Pub/Sub message subscriber — receives cross-instance broadcasts, forwards to local WebSocket
 *
 * <p>订阅频道: atlas:message:push /
 * Subscription channel: atlas:message:push</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisMessageSubscriber {

    private final MessageTemplate messageTemplate;

    /**
     * 处理收到的 Redis 消息 / Handle received Redis message
     *
     * <p>收到消息后，通过本地 MessageTemplate 再次推送到本实例上的 WebSocket 连接 /
     * After receiving a message, re-push via local MessageTemplate to local WebSocket connections</p>
     */
    @SuppressWarnings("unchecked")
    public void onMessage(Map<String, Object> payload) {
        try {
            // 提取关键字段 / Extract key fields
            Object supplierIdObj = payload.get("supplierId");
            Object typeObj = payload.get("type");

            // 移除内部标记（避免透明传递到客户端） / Remove internal marker
            payload.remove("_redis_broadcast");

            if (supplierIdObj instanceof Number supplierId) {
                // 点对点推送到指定供应商 / Point-to-point push to specific supplier
                messageTemplate.sendToSupplier(
                        supplierId.longValue(),
                        "/queue/message",
                        payload
                );
                log.debug("Redis Pub/Sub → 本地 WebSocket 转发: supplierId={}, type={}",
                        supplierId, typeObj);
            } else {
                // 广播（无 supplierId 时） / Broadcast (when no supplierId)
                messageTemplate.broadcastToAllSuppliers("/topic/announcement", payload);
                log.debug("Redis Pub/Sub → 本地广播: type={}", typeObj);
            }
        } catch (Exception e) {
            log.error("Redis Pub/Sub 消息处理失败: {}", e.getMessage(), e);
        }
    }
}
