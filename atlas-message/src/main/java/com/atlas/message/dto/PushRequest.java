package com.atlas.message.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 推送请求 DTO — 供其他微服务通过 Feign 调用，支持 WebSocket / 邮件 / 短信三通道 /
 * Push request DTO — for other microservices to call via Feign, supports WebSocket / Mail / SMS triple channels
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PushRequest {

    /** 消息标题 / Message title */
    private String title;

    /** 消息内容 / Message content */
    private String content;

    /** 消息类型: ORDER / DELIVERY / SETTLEMENT / SYSTEM / APPROVAL / QUALITY */
    private String type;

    /** 关联业务ID（如订单号 PO-2026-0001）/ Related business ID (e.g. order number) */
    private String relatedId;

    /** 关联业务类型 / Related business type */
    private String relatedType;

    /** 目标供应商ID / Target supplier ID */
    private Long supplierId;

    /** 目标用户ID（内部用户，可选）/ Target user ID (internal user, optional) */
    private Long userId;

    /** 推送渠道，默认 WEBSOCKET / Push channel, default WEBSOCKET */
    @Builder.Default
    private String channel = "WEBSOCKET";

    /** 模板编码（如使用模板推送）/ Template code (if using template push) */
    private String templateCode;

    /** 模板占位符替换参数 / Template placeholder substitution parameters */
    private Map<String, Object> templateParams;

    // ---- 邮件通道字段 / Mail channel fields ----

    /** 是否同时推送邮件 / Whether to also send by email */
    private boolean mailNotify;

    /** 收件人邮箱 / Recipient email */
    private String email;

    /** 邮件模板名称（可选，覆盖 templateCode）/ Mail template name (optional, overrides templateCode) */
    private String mailTemplateName;

    // ---- 短信通道字段 / SMS channel fields ----

    /** 是否同时推送短信 / Whether to also send by SMS */
    private boolean smsNotify;

    /** 收件人手机号 / Recipient phone number */
    private String phone;

    /** 短信模板编码（可选，覆盖 templateCode）/ SMS template code (optional, overrides templateCode) */
    private String smsTemplateCode;

    /** 短信模板参数 / SMS template parameters */
    private Map<String, String> smsTemplateParams;
}
