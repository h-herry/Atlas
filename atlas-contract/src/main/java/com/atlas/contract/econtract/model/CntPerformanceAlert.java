package com.atlas.contract.econtract.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 履约提醒实体 — 对应 cnt_performance_alert 表 /
 * Performance alert entity — corresponds to cnt_performance_alert table
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("cnt_performance_alert")
public class CntPerformanceAlert {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 履约指标ID / Performance ID */
    private Long performanceId;

    /** 提醒类型: DUE_SOON/OVERDUE/BREACH / Alert type */
    private String alertType;

    /** 提醒消息 / Alert message */
    private String alertMessage;

    /** 通知对象 / Notify target */
    private String notifyTo;

    /** 是否已发送: 0-未发送 1-已发送 / Sent: 0-no 1-yes */
    private Integer isSent;

    /** 发送时间 / Sent at */
    private LocalDateTime sentAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
