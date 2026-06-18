package com.atlas.common.message.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 消息渠道偏好配置实体 — 用户级 [消息类型 → 渠道] 偏好矩阵 /
 * Message channel preference entity — user-level [message type → channel] preference matrix
 *
 * <p>每种消息类型可独立配置 WebSocket / 邮件 / 短信开关，支持静默时段 /
 * Each message type independently configurable for WebSocket / Mail / SMS toggles with quiet hours</p>
 *
 * @since 1.2.22
 */
@Data
@TableName("msg_channel_preference")
public class MsgChannelPreference {

    @TableId(type = IdType.ASSIGN_ID)
    private Long prefId;

    private Long userId;

    /** 消息事件类型: ORDER / DELIVERY / SETTLEMENT / SYSTEM / APPROVAL / QUALITY */
    private String eventType;

    /** WebSocket 通道启用 / WebSocket enabled */
    private Integer wsEnabled;

    /** 邮件通道启用 / Mail enabled */
    private Integer mailEnabled;

    /** 短信通道启用 / SMS enabled */
    private Integer smsEnabled;

    /** 静默时段起始 / Quiet hours start */
    private LocalTime quietStart;

    /** 静默时段截止 / Quiet hours end */
    private LocalTime quietEnd;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
