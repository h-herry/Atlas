package com.atlas.common.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 消息发送模板类 — 封装 SimpMessagingTemplate，提供点对点与广播推送 /
 * Message sending template class — wraps SimpMessagingTemplate, provides point-to-point and broadcast push
 *
 * <p>使用场景: /
 * Usage scenarios:
 * <ul>
 *   <li>sendToSupplier — 向特定供应商推送消息（点对点） / Push to specific supplier (P2P)</li>
 *   <li>broadcastToAllSuppliers — 向全部供应商广播 / Broadcast to all suppliers</li>
 *   <li>sendToUser — 向内部用户推送（如审批通知）/ Push to internal user (e.g. approval notification)</li>
 * </ul>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MessageTemplate {

    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 向特定供应商推送消息（点对点） /
     * Push message to a specific supplier (point-to-point)
     *
     * @param supplierId  供应商ID / Supplier ID
     * @param destination 目标路径（如 "/queue/order"） / Destination path (e.g. "/queue/order")
     * @param payload     消息体 / Message payload
     */
    public void sendToSupplier(Long supplierId, String destination, Object payload) {
        String target = "/queue/supplier/" + supplierId + destination;
        messagingTemplate.convertAndSend(target, payload);
        log.debug("消息已推送到供应商: supplierId={}, destination={}", supplierId, target);
    }

    /**
     * 向全部供应商广播消息 /
     * Broadcast message to all suppliers
     *
     * @param destination 目标路径（如 "/topic/announcement"） / Destination path (e.g. "/topic/announcement")
     * @param payload     消息体 / Message payload
     */
    public void broadcastToAllSuppliers(String destination, Object payload) {
        String target = "/topic/supplier" + destination;
        messagingTemplate.convertAndSend(target, payload);
        log.debug("消息已广播至全部供应商: destination={}", target);
    }

    /**
     * 向特定内部用户推送消息（基于 Spring Security Principal） /
     * Push message to a specific internal user (based on Spring Security Principal)
     *
     * @param userId      用户ID / User ID
     * @param destination 目标路径（如 "/queue/approval"） / Destination path (e.g. "/queue/approval")
     * @param payload     消息体 / Message payload
     */
    public void sendToUser(Long userId, String destination, Object payload) {
        String target = "/queue/user/" + userId + destination;
        messagingTemplate.convertAndSend(target, payload);
        log.debug("消息已推送到用户: userId={}, destination={}", userId, target);
    }

    /**
     * 发送带消息头的消息 / Send message with headers
     */
    public void sendToSupplier(Long supplierId, String destination, Object payload, Map<String, Object> headers) {
        String target = "/queue/supplier/" + supplierId + destination;
        messagingTemplate.convertAndSend(target, payload, headers);
        log.debug("带头的消息已推送到供应商: supplierId={}, destination={}", supplierId, target);
    }
}
