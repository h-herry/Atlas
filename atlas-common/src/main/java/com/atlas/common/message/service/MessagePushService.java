package com.atlas.common.message.service;

import com.atlas.common.websocket.MessageTemplate;
import com.atlas.common.message.dto.PushRequest;
import com.atlas.common.message.mail.service.MailService;
import com.atlas.common.message.mail.template.MailTemplateEnum;
import com.atlas.common.message.mapper.DeadMsgRecordMapper;
import com.atlas.common.message.model.DeadMsgRecord;
import com.atlas.common.message.model.Message;
import com.atlas.common.message.model.MsgChannelPreference;
import com.atlas.common.message.sms.service.SmsService;
import com.atlas.common.message.sms.template.SmsTemplateEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 消息推送编排 Service — WebSocket / 邮件 / 短信三通道统一编排层 /
 * Message push orchestration Service — unified orchestration layer for WebSocket / Mail / SMS triple channels
 *
 * <h3>核心流程 / Core flow</h3>
 * <ol>
 *   <li>接收 PushRequest → 构建 Message 实体 / Receive PushRequest → build Message entity</li>
 *   <li>持久化到 msg_record 表 / Persist to msg_record table</li>
 *   <li>通过 WebSocket 推送到在线供应商（所有消息默认通道）/ Push to online supplier via WebSocket (default channel for all messages)</li>
 *   <li>根据 mailNotify 标识发送邮件（重要消息）/ Send email based on mailNotify flag (important messages)</li>
 *   <li>根据 smsNotify 标识发送短信（紧急消息）/ Send SMS based on smsNotify flag (urgent messages)</li>
 *   <li>通过 Redis Pub/Sub 广播到其他 atlas-message 实例 / Broadcast to other instances via Redis Pub/Sub</li>
 * </ol>
 *
 * <h3>消息通道选择策略 / Message Channel Selection Strategy</h3>
 * <pre>
 * | 消息类型 / Message Type          | WebSocket | 邮件 / Mail | 短信 / SMS |
 * |---------------------------------|-----------|-------------|------------|
 * | 订单创建 / ORDER_CREATED         | ✓         | ✓           | ✓          |
 * | 订单确认 / ORDER_CONFIRMED       | ✓         | ✗           | ✗          |
 * | 交期预警 / DELIVERY_DELAYED      | ✓         | ✓           | ✓          |
 * | 发货通知 / DELIVERY_SHIPPED      | ✓         | ✓           | ✓          |
 * | 竞价邀请 / BIDDING_INVITATION    | ✓         | ✓           | ✓          |
 * | 竞价结果 / BIDDING_RESULT        | ✓         | ✓           | ✗          |
 * | 入驻审核 / SUPPLIER_APPROVED     | ✗         | ✓           | ✓          |
 * | 合同签署 / CONTRACT_SIGNING      | ✓         | ✓           | ✓          |
 * | 质检不合格 / QUALITY_INSPECT_FAIL| ✓         | ✓           | ✗          |
 * </pre>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessagePushService {

    private final MessageTemplate messageTemplate;
    private final MessageService messageService;
    private final WebSocketSessionManager sessionManager;
    private final RedisTemplate<String, Object> redisTemplate;
    private final MailService mailService;
    private final SmsService smsService;
    private final DeadMsgRecordMapper deadMsgRecordMapper;
    private final MsgChannelPreferenceService channelPreferenceService;

    /**
     * Redis Pub/Sub 频道（跨实例消息广播） / Redis Pub/Sub channel (cross-instance message broadcast)
     */
    private static final String MESSAGE_CHANNEL = "atlas:message:push";

    // ================================================================
    // 统一推送入口 / Unified push entry
    // ================================================================

    /**
     * 统一推送入口 — 根据 PushRequest 中 mailNotify / smsNotify 标志自动编排三通道 /
     * Unified push entry — automatically orchestrates triple channels based on mailNotify / smsNotify flags in PushRequest
     *
     * <p>所有消息默认走 WebSocket 推送；邮件和短信按需启用 /
     * All messages go through WebSocket by default; mail and SMS are enabled on demand</p>
     *
     * @param request 推送请求（含通道选择标志）/ Push request (with channel selection flags)
     */
    @Transactional(rollbackFor = Exception.class)
    public void push(PushRequest request) {
        // 0. 渠道偏好过滤（P1-3.9.5）/ Channel preference filtering
        boolean wsEnabled = true;  // WebSocket is always default
        boolean mailEnabled = request.isMailNotify();
        boolean smsEnabled = request.isSmsNotify();

        if (request.getUserId() != null) {
            MsgChannelPreference pref = channelPreferenceService.getByUserAndEvent(
                    request.getUserId(), request.getType());
            if (pref != null) {
                wsEnabled = pref.getWsEnabled() == null || pref.getWsEnabled() == 1;
                mailEnabled = mailEnabled && (pref.getMailEnabled() != null && pref.getMailEnabled() == 1);
                smsEnabled = smsEnabled && (pref.getSmsEnabled() != null && pref.getSmsEnabled() == 1);
            }
        }

        // 1. 构建并持久化消息 / Build and persist message
        Message msg = buildMessage(request);
        messageService.save(msg);
        Map<String, Object> payload = buildPayload(msg);

        // 2. WebSocket 实时推送（所有消息默认通道，受偏好过滤）/ WebSocket real-time push (default, filtered by preference)
        if (wsEnabled) {
            pushViaWebSocket(request, payload);
        }

        // 3. 邮件通道（重要消息，异步，受偏好过滤）/ Mail channel (important, async, filtered by preference)
        if (mailEnabled) {
            pushViaMail(request);
        }

        // 4. 短信通道（紧急消息，异步，受偏好过滤）/ SMS channel (urgent, async, filtered by preference)
        if (smsEnabled) {
            pushViaSms(request);
        }

        // 5. Redis Pub/Sub 跨实例广播 / Redis Pub/Sub cross-instance broadcast
        broadcastViaRedis(payload, request.getType());
    }

    /**
     * 推送消息到指定供应商（兼容旧接口，默认仅 WebSocket）/ Push message to a specific supplier (legacy, WebSocket only by default)
     */
    @Transactional(rollbackFor = Exception.class)
    public void pushToSupplier(PushRequest request) {
        // 如果 mailNotify/smsNotify 已设置则走统一入口 / If mailNotify/smsNotify are set, use unified entry
        if (request.isMailNotify() || request.isSmsNotify()) {
            push(request);
            return;
        }
        // 仅 WebSocket / WebSocket only
        Message msg = buildMessage(request);
        messageService.save(msg);
        Map<String, Object> payload = buildPayload(msg);
        pushViaWebSocket(request, payload);
        broadcastViaRedis(payload, request.getType());
    }

    /**
     * 向全部供应商广播消息 / Broadcast message to all suppliers
     */
    @Transactional(rollbackFor = Exception.class)
    public void broadcastToAllSuppliers(PushRequest request) {
        Message msg = buildMessage(request);
        messageService.save(msg);

        Map<String, Object> payload = buildPayload(msg);
        messageTemplate.broadcastToAllSuppliers("/topic/announcement", payload);

        // Redis Pub/Sub 跨实例广播是尽力而为通知，失败不应影响消息持久化主流程 /
        // Redis Pub/Sub cross-instance broadcast is best-effort; failure must not affect message persistence
        try {
            redisTemplate.convertAndSend(MESSAGE_CHANNEL, payload);
        } catch (Exception e) {
            log.warn("Redis Pub/Sub 广播失败（非致命）: {}", e.getMessage());
        }
    }

    /**
     * 推送消息到内部用户 / Push message to internal user
     */
    @Transactional(rollbackFor = Exception.class)
    public void pushToUser(PushRequest request) {
        Message msg = buildMessage(request);
        messageService.save(msg);

        Map<String, Object> payload = buildPayload(msg);
        if (request.getUserId() != null && sessionManager.isUserOnline(request.getUserId())) {
            messageTemplate.sendToUser(request.getUserId(), "/queue/message", payload);
        }

        // Redis Pub/Sub 跨实例广播是尽力而为通知，失败不应影响消息持久化主流程 /
        // Redis Pub/Sub cross-instance broadcast is best-effort; failure must not affect message persistence
        try {
            redisTemplate.convertAndSend(MESSAGE_CHANNEL, payload);
        } catch (Exception e) {
            log.warn("Redis Pub/Sub 广播失败（非致命）: {}", e.getMessage());
        }
    }

    // ================================================================
    // 通道推送实现 / Channel push implementations
    // ================================================================

    /**
     * WebSocket 通道推送 / WebSocket channel push
     */
    private void pushViaWebSocket(PushRequest request, Map<String, Object> payload) {
        if (request.getSupplierId() != null && sessionManager.isSupplierOnline(request.getSupplierId())) {
            messageTemplate.sendToSupplier(request.getSupplierId(), "/queue/message", payload);
            log.info("WebSocket 消息已推送: supplierId={}, type={}, title={}",
                    request.getSupplierId(), request.getType(), request.getTitle());
        } else {
            log.info("供应商离线，消息已持久化待拉取: supplierId={}, type={}",
                    request.getSupplierId(), request.getType());
        }
    }

    /**
     * 邮件通道推送（异步 + 重试 + 死信） / Mail channel push (async + retry + dead-letter)
     *
     * <p>重试策略: 失败后间隔 1min / 5min / 15min 重试，最多 3 次；3 次后移入 dead_msg_record。 /
     * Retry strategy: retry after 1min / 5min / 15min intervals, max 3 retries; move to dead_msg_record after 3.</p>
     */
    @Async
    public void pushViaMail(PushRequest request) {
        String email = request.getEmail();
        if (email == null || email.isBlank()) {
            log.warn("邮件发送跳过，收件人邮箱为空: supplierId={}, type={}",
                    request.getSupplierId(), request.getType());
            return;
        }

        String templateName = resolveMailTemplateName(request);
        if (templateName == null) {
            log.warn("邮件发送跳过，未找到匹配的邮件模板: type={}", request.getType());
            return;
        }

        int maxRetries = 3;
        long[] delays = {60_000, 300_000, 900_000}; // 1min / 5min / 15min

        for (int retry = 0; retry <= maxRetries; retry++) {
            try {
                Map<String, Object> variables = buildMailVariables(request);
                MailTemplateEnum template = MailTemplateEnum.fromCode(templateName);
                String subject = template != null ? template.getTitle() : request.getTitle();

                mailService.sendTemplateMail(email, subject, templateName, variables);
                log.info("邮件已发送: to={}, template={}, type={}, retry={}", email, templateName, request.getType(), retry);
                return; // 成功则退出 / Exit on success
            } catch (Exception e) {
                log.error("邮件发送失败 (重试 {}/{}): to={}, type={}, title={}",
                        retry, maxRetries, email, request.getType(), request.getTitle(), e);

                if (retry < maxRetries) {
                    try {
                        Thread.sleep(delays[retry]);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    // 3 次重试后移入死信表 / Move to dead-letter table after 3 retries
                    saveToDeadQueue(request, "MAIL", email, retry, e.getMessage());
                }
            }
        }
    }

    /**
     * 短信通道推送（异步 + 重试 + 死信） / SMS channel push (async + retry + dead-letter)
     *
     * <p>重试策略: 失败后间隔 1min / 5min / 15min 重试，最多 3 次；3 次后移入 dead_msg_record。 /
     * Retry strategy: retry after 1min / 5min / 15min intervals, max 3 retries; move to dead_msg_record after 3.</p>
     */
    @Async
    public void pushViaSms(PushRequest request) {
        String phone = request.getPhone();
        if (phone == null || phone.isBlank()) {
            log.warn("短信发送跳过，收件人手机号为空: supplierId={}, type={}",
                    request.getSupplierId(), request.getType());
            return;
        }

        String templateCode = resolveSmsTemplateCode(request);
        if (templateCode == null) {
            log.warn("短信发送跳过，未找到匹配的短信模板: type={}", request.getType());
            return;
        }

        int maxRetries = 3;
        long[] delays = {60_000, 300_000, 900_000}; // 1min / 5min / 15min

        for (int retry = 0; retry <= maxRetries; retry++) {
            try {
                Map<String, String> params = buildSmsParams(request);
                smsService.sendSms(phone, templateCode, params);
                log.info("短信已发送: phone={}, templateCode={}, type={}, retry={}", phone, templateCode, request.getType(), retry);
                return; // 成功则退出 / Exit on success
            } catch (Exception e) {
                log.error("短信发送失败 (重试 {}/{}): phone={}, type={}, title={}",
                        retry, maxRetries, maskPhone(phone), request.getType(), request.getTitle(), e);

                if (retry < maxRetries) {
                    try {
                        Thread.sleep(delays[retry]);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                } else {
                    // 3 次重试后移入死信表 / Move to dead-letter table after 3 retries
                    saveToDeadQueue(request, "SMS", phone, retry, e.getMessage());
                }
            }
        }
    }

    /**
     * 将失败消息持久化到死信表 / Persist failed message to dead-letter table
     *
     * @param request   原始推送请求 / Original push request
     * @param channel   推送通道 (MAIL / SMS) / Push channel
     * @param recipient 接收人地址 / Recipient address
     * @param retries   已重试次数 / Retries performed
     * @param errorMsg  错误信息 / Error message
     */
    private void saveToDeadQueue(PushRequest request, String channel, String recipient, int retries, String errorMsg) {
        try {
            DeadMsgRecord dead = new DeadMsgRecord();
            dead.setOriginalMsgId(request.getRelatedId());
            dead.setSupplierId(request.getSupplierId());
            dead.setUserId(request.getUserId());
            dead.setTitle(request.getTitle());
            dead.setContent(request.getContent());
            dead.setType(request.getType());
            dead.setChannel(channel);
            dead.setRecipient(recipient);
            dead.setRetryCount(retries);
            dead.setLastError(errorMsg);
            dead.setFailedAt(LocalDateTime.now());
            dead.setCreatedAt(LocalDateTime.now());
            deadMsgRecordMapper.insert(dead);
            log.warn("消息已移入死信队列: channel={}, recipient={}, retries={}", channel, recipient, retries);
        } catch (Exception e) {
            log.error("死信记录写入失败（最终兜底）: channel={}, recipient={}", channel, recipient, e);
        }
    }

    /**
     * Redis Pub/Sub 跨实例广播 / Redis Pub/Sub cross-instance broadcast
     */
    private void broadcastViaRedis(Map<String, Object> payload, String type) {
        try {
            Map<String, Object> broadcastPayload = new HashMap<>(payload);
            broadcastPayload.put("_redis_broadcast", true);
            redisTemplate.convertAndSend(MESSAGE_CHANNEL, broadcastPayload);
            log.debug("Redis Pub/Sub 广播已发送: channel={}, type={}", MESSAGE_CHANNEL, type);
        } catch (Exception e) {
            log.warn("Redis Pub/Sub 广播失败（非致命）: {}", e.getMessage());
        }
    }

    // ================================================================
    // 内部辅助方法 / Internal helper methods
    // ================================================================

    /**
     * 从 PushRequest 构建 Message 实体 / Build Message entity from PushRequest
     */
    private Message buildMessage(PushRequest request) {
        Message msg = new Message();
        msg.setSupplierId(request.getSupplierId());
        msg.setUserId(request.getUserId());
        msg.setTitle(request.getTitle());
        msg.setContent(request.getContent());
        msg.setType(request.getType());
        msg.setRelatedId(request.getRelatedId());
        msg.setRelatedType(request.getRelatedType());
        msg.setChannel(request.getChannel() != null ? request.getChannel() : "WEBSOCKET");
        msg.setPriority(request.getPriority() != null ? request.getPriority() : 2);
        msg.setIsRead(0);
        msg.setCreatedAt(LocalDateTime.now());
        return msg;
    }

    /**
     * 构建前端推送负载 / Build frontend push payload
     */
    private Map<String, Object> buildPayload(Message msg) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("id", msg.getId());
        payload.put("title", msg.getTitle());
        payload.put("content", msg.getContent());
        payload.put("type", msg.getType());
        payload.put("relatedId", msg.getRelatedId());
        payload.put("relatedType", msg.getRelatedType());
        payload.put("supplierId", msg.getSupplierId());
        payload.put("createdAt", msg.getCreatedAt() != null ? msg.getCreatedAt().toString() : null);
        return payload;
    }

    /**
     * 解析邮件模板名称 / Resolve mail template name
     *
     * <p>优先级: mailTemplateName（显式指定）> templateCode > type 映射 / Priority: mailTemplateName (explicit) > templateCode > type mapping</p>
     */
    private String resolveMailTemplateName(PushRequest request) {
        // 1. 用户显式指定 / User explicitly specified
        if (request.getMailTemplateName() != null && !request.getMailTemplateName().isBlank()) {
            return request.getMailTemplateName();
        }
        // 2. 通过 templateCode 映射 / Map via templateCode
        if (request.getTemplateCode() != null && !request.getTemplateCode().isBlank()) {
            return request.getTemplateCode();
        }
        // 3. 通过 type 映射 / Map via type
        return messageTypeToMailTemplate(request.getType());
    }

    /**
     * 解析短信模板编码 / Resolve SMS template code
     *
     * <p>优先级: smsTemplateCode（显式指定）> templateCode > type 映射 / Priority: smsTemplateCode (explicit) > templateCode > type mapping</p>
     */
    private String resolveSmsTemplateCode(PushRequest request) {
        // 1. 用户显式指定 / User explicitly specified
        if (request.getSmsTemplateCode() != null && !request.getSmsTemplateCode().isBlank()) {
            return request.getSmsTemplateCode();
        }
        // 2. 通过 templateCode 映射 / Map via templateCode
        if (request.getTemplateCode() != null && !request.getTemplateCode().isBlank()) {
            SmsTemplateEnum templateEnum = SmsTemplateEnum.fromCode(request.getTemplateCode());
            if (templateEnum != null) {
                return templateEnum.getAliyunTemplateCode();
            }
        }
        // 3. 通过 type 映射 / Map via type
        SmsTemplateEnum templateEnum = messageTypeToSmsTemplate(request.getType());
        return templateEnum != null ? templateEnum.getAliyunTemplateCode() : null;
    }

    /**
     * 构建邮件模板变量 / Build mail template variables
     */
    private Map<String, Object> buildMailVariables(PushRequest request) {
        Map<String, Object> variables = new HashMap<>();
        if (request.getTemplateParams() != null) {
            variables.putAll(request.getTemplateParams());
        }
        variables.putIfAbsent("title", request.getTitle());
        variables.putIfAbsent("content", request.getContent());
        variables.putIfAbsent("type", request.getType());
        variables.putIfAbsent("relatedId", request.getRelatedId());
        variables.putIfAbsent("supplierId", request.getSupplierId());
        return variables;
    }

    /**
     * 构建短信模板参数 / Build SMS template parameters
     */
    private Map<String, String> buildSmsParams(PushRequest request) {
        Map<String, String> params = new HashMap<>();
        if (request.getSmsTemplateParams() != null) {
            params.putAll(request.getSmsTemplateParams());
        }
        if (request.getTemplateParams() != null) {
            request.getTemplateParams().forEach((k, v) -> params.putIfAbsent(k, v != null ? v.toString() : ""));
        }
        return params;
    }

    /**
     * 消息类型 → 邮件模板映射 / Message type → Mail template mapping
     */
    private String messageTypeToMailTemplate(String type) {
        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "ORDER", "ORDER_CREATED" -> "ORDER_CREATED";
            case "ORDER_CONFIRMED" -> "ORDER_CONFIRMED";
            case "ORDER_STATUS_CHANGED" -> "ORDER_CONFIRMED";
            case "DELIVERY_SHIPPED" -> "DELIVERY_SHIPPED";
            case "DELIVERY_DELAYED", "DELIVERY_APPROACHING", "DELIVERY_OVERDUE" -> "DELIVERY_DELAYED";
            case "CONTRACT_SIGNING" -> "CONTRACT_SIGNING";
            case "BIDDING_INVITATION", "BIDDING_INVITE" -> "BIDDING_INVITATION";
            case "BIDDING_RESULT" -> "BIDDING_RESULT";
            case "QUALITY_INSPECTION_FAIL", "QUALITY" -> "QUALITY_INSPECTION_FAIL";
            case "SETTLEMENT_STATEMENT", "SETTLEMENT" -> "SETTLEMENT_STATEMENT";
            case "SUPPLIER_REGISTER" -> "SUPPLIER_REGISTER_SUCCESS";
            case "SUPPLIER_APPROVED", "APPROVAL" -> "SUPPLIER_APPROVED";
            case "SUPPLIER_REJECTED" -> "SUPPLIER_REJECTED";
            default -> null;
        };
    }

    /**
     * 消息类型 → 短信模板映射 / Message type → SMS template mapping
     */
    private SmsTemplateEnum messageTypeToSmsTemplate(String type) {
        if (type == null) return null;
        return switch (type.toUpperCase()) {
            case "ORDER", "ORDER_CREATED" -> SmsTemplateEnum.SMS_ORDER_CREATED;
            case "DELIVERY_SHIPPED", "ORDER_DELIVERY" -> SmsTemplateEnum.SMS_ORDER_DELIVERY;
            case "DELIVERY_DELAYED", "DELIVERY_APPROACHING", "DELIVERY_OVERDUE" -> SmsTemplateEnum.SMS_DELIVERY_DELAY;
            case "BIDDING_INVITATION", "BIDDING_INVITE" -> SmsTemplateEnum.SMS_BIDDING_INVITE;
            case "BIDDING_RESULT" -> SmsTemplateEnum.SMS_BIDDING_RESULT;
            case "CONTRACT_SIGNING" -> SmsTemplateEnum.SMS_CONTRACT_SIGN;
            case "SUPPLIER_REGISTER" -> SmsTemplateEnum.SMS_SUPPLIER_REG;
            case "SUPPLIER_APPROVED", "APPROVAL" -> SmsTemplateEnum.SMS_SUPPLIER_APPROVED;
            default -> null;
        };
    }

    /**
     * 手机号脱敏：保留前3后4，中间替换为**** /
     * Mask phone number: keep first 3 and last 4 digits, replace middle with ****
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}
