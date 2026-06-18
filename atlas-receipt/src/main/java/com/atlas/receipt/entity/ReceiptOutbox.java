package com.atlas.receipt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货确认本地消息表 — 保障 MQ 发送失败时可定时补偿 /
 * Receipt outbox (local message table) — ensures compensation on MQ send failure
 */
@Data
@TableName("receipt_outbox")
public class ReceiptOutbox {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long receiptId;

    private String messageTopic;

    private String messageTag;

    private String messageBody;

    /** 0-待发送 1-已发送 2-发送失败 / 0-pending 1-sent 2-failed */
    private Integer status;

    private Integer retryCount;

    private LocalDateTime nextRetryTime;

    private String errorMsg;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
