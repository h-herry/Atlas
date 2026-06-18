package com.atlas.common.message.event;

import com.atlas.common.websocket.MessageTemplate;
import com.atlas.common.message.model.Message;
import com.atlas.common.message.service.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息事件监听器 — 监听业务事件，自动触发消息推送 /
 * Message event listener — listens for business events and triggers message push automatically
 *
 * <p>监听方式: Spring ApplicationEvent + 内部事件发布 /
 * Listening mode: Spring ApplicationEvent + internal event publishing
 *
 * <p>业务节点的 Service 层发布 {@link MessagePushEvent}，本监听器负责持久化 + 推送 /
 * Service layers at business nodes publish {@link MessagePushEvent}, this listener handles persistence + push</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageEventListener {

    private final MessageService messageService;
    private final MessageTemplate messageTemplate;

    /**
     * 处理消息推送事件 / Handle message push event
     *
     * <p>流程: 持久化 → WebSocket 实时推送 / Flow: persist → WebSocket real-time push</p>
     */
    @EventListener
    public void handleMessagePushEvent(MessagePushEvent event) {
        try {
            // 1. 持久化 / Persist
            Message msg = new Message();
            msg.setSupplierId(event.getSupplierId());
            msg.setUserId(event.getUserId());
            msg.setTitle(event.getTitle());
            msg.setContent(event.getContent());
            msg.setType(event.getType());
            msg.setRelatedId(event.getRelatedId());
            msg.setRelatedType(event.getRelatedType());
            msg.setChannel("WEBSOCKET");
            msg.setIsRead(0);
            msg.setCreatedAt(LocalDateTime.now());
            messageService.save(msg);

            // 2. 构建推送负载 / Build push payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("id", msg.getId());
            payload.put("title", msg.getTitle());
            payload.put("content", msg.getContent());
            payload.put("type", msg.getType());
            payload.put("relatedId", msg.getRelatedId());
            payload.put("relatedType", msg.getRelatedType());
            payload.put("supplierId", msg.getSupplierId());
            payload.put("createdAt", msg.getCreatedAt().toString());

            // 3. WebSocket 推送 / WebSocket push
            if (event.getSupplierId() != null) {
                messageTemplate.sendToSupplier(event.getSupplierId(), "/queue/message", payload);
            }
            if (event.getUserId() != null) {
                messageTemplate.sendToUser(event.getUserId(), "/queue/message", payload);
            }

            log.info("消息事件已处理: type={}, supplierId={}, title={}",
                    event.getType(), event.getSupplierId(), event.getTitle());
        } catch (Exception e) {
            log.error("消息事件处理失败: type={}, error={}", event.getType(), e.getMessage(), e);
        }
    }
}
