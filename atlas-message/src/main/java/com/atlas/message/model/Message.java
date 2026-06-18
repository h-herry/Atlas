package com.atlas.message.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 消息记录实体 — 对应 msg_record 表 /
 * Message record entity — maps to msg_record table
 *
 * <p>记录每一条通过 WebSocket / Email / SMS 渠道推送的消息，支持已读/未读状态追踪 /
 * Records every message pushed via WebSocket / Email / SMS channels with read/unread status tracking</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("msg_record")
public class Message {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 接收用户ID（内部用户） / Recipient user ID (internal user) */
    private Long userId;

    /** 消息标题 / Message title */
    private String title;

    /** 消息内容 / Message content */
    private String content;

    /** 消息类型: ORDER / DELIVERY / SETTLEMENT / SYSTEM / APPROVAL / QUALITY /
     * Message type: ORDER / DELIVERY / SETTLEMENT / SYSTEM / APPROVAL / QUALITY */
    private String type;

    /** 关联业务ID / Related business ID */
    private String relatedId;

    /** 关联业务类型 / Related business type */
    private String relatedType;

    /** 推送渠道: WEBSOCKET / EMAIL / SMS / Push channel: WEBSOCKET / EMAIL / SMS */
    private String channel;

    /** 是否已读: 0 未读 / 1 已读 / Is read: 0 unread / 1 read */
    private Integer isRead;

    /** 已读时间 / Read timestamp */
    private LocalDateTime readAt;

    /** 创建时间 / Creation timestamp */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    // ---- 域方法 / Domain methods ----

    /** 标记为已读 / Mark as read */
    public void markAsRead() {
        this.isRead = 1;
        this.readAt = LocalDateTime.now();
    }

    /** 是否未读 / Is unread */
    public boolean isUnread() {
        return this.isRead == null || this.isRead == 0;
    }
}
