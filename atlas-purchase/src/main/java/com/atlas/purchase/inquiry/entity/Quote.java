package com.atlas.purchase.inquiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报价记录实体 / Quote entity
 *
 * <p>供应商针对某一询价单提交的完整报价，包含总金额和报价状态。 /
 * Supplier's full quote for an inquiry, containing total amount and quote status.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("quote")
public class Quote {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联询价单ID / Associated inquiry ID */
    private Long inquiryId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 报价总金额 / Total quote amount */
    private BigDecimal totalAmount;

    /** 状态: SUBMITTED / WITHDRAWN / ACCEPTED / REJECTED */
    private String status;

    /** 提交时间 / Submit time */
    private LocalDateTime submitTime;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
