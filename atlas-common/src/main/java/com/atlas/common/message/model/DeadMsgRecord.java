package com.atlas.common.message.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 死信消息记录实体 / Dead message record entity
 *
 * <p>消息推送重试 3 次仍失败后移入 dead_msg_record 表。 /
 * Messages that fail after 3 retries are moved to dead_msg_record table.</p>
 *
 * @author Atlas Team
 * @since 1.2.21
 */
@Data
@TableName("dead_msg_record")
public class DeadMsgRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 原始消息关联业务ID（如订单号 PO-2026-0001）/ Original message related business ID */
    private String originalMsgId;

    /** 供应商 ID / Supplier ID */
    private Long supplierId;

    /** 用户 ID / User ID */
    private Long userId;

    /** 消息标题 / Message title */
    private String title;

    /** 消息内容 / Message content */
    private String content;

    /** 消息类型 / Message type */
    private String type;

    /** 推送通道: MAIL / SMS / WEBSOCKET / Push channel */
    private String channel;

    /** 接收人地址（邮箱/手机号）/ Recipient address (email/phone) */
    private String recipient;

    /** 已重试次数 / Retry count */
    private Integer retryCount;

    /** 最后一次错误信息 / Last error message */
    private String lastError;

    /** 最终失败时间 / Final failure time */
    private LocalDateTime failedAt;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
