package com.atlas.contract.econtract.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 履约跟踪实体 — 对应 cnt_performance 表 /
 * Performance tracking entity — corresponds to cnt_performance table
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("cnt_performance")
public class CntPerformance {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 合同ID / Contract ID */
    private Long contractId;

    /** 条款引用(如 第3.2条) / Clause reference (e.g. Art 3.2) */
    private String clauseRef;

    /** 履约指标名称 / Performance metric name */
    private String metricName;

    /** 指标类型: DELIVERY/PAYMENT/QUALITY/MILESTONE / Metric type */
    private String metricType;

    /** 目标值 / Target value */
    private String targetValue;

    /** 实际值 / Actual value */
    private String actualValue;

    /** 履约状态: NOT_STARTED/IN_PROGRESS/COMPLETED/BREACHED / Performance status */
    private String status;

    /** 到期日期 / Due date */
    private LocalDate dueDate;

    /** 完成时间 / Completed at */
    private LocalDateTime completedAt;

    /** 备注 / Remark */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
