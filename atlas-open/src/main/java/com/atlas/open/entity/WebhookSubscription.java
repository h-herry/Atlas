package com.atlas.open.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Webhook 事件订阅 / Webhook event subscription
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("webhook_subscription")
public class WebhookSubscription {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 客户端ID / Client ID */
    private Long clientId;

    /** 事件类型: ORDER_CREATED/ORDER_STATUS_CHANGE/RECEIPT_CONFIRMED/INVOICE_UPLOADED /
     *  Event type: ORDER_CREATED/ORDER_STATUS_CHANGE/RECEIPT_CONFIRMED/INVOICE_UPLOADED */
    private String eventType;

    /** 回调 URL / Callback URL */
    private String callbackUrl;

    /** Webhook 签名密钥 / Webhook signing secret */
    private String secret;

    /** 状态: 1启用 0禁用 / Status: 1-enabled 0-disabled */
    private Integer status;

    /** 创建时间 / Creation time */
    private LocalDateTime createdAt;
}
