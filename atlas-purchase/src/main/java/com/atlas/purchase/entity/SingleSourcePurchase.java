package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 单一来源采购实体 / Single-source procurement entity
 * <p>
 * 不经过竞争直接与唯一供应商谈判采购。 /
 * Direct negotiation with a sole supplier without competitive procedures.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("single_source_purchase")
public class SingleSourcePurchase {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联采购单ID / Associated purchase order ID */
    private Long purchaseOrderId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 单一来源理由 / Single-source justification */
    private String singleSourceReason;

    /** 谈判金额 / Negotiation amount */
    private BigDecimal negotiationAmount;

    /** 最终成交金额 / Final transaction amount */
    private BigDecimal finalAmount;

    /** 状态: 0-草稿 1-谈判中 2-已成交 3-终止 / Status: 0-draft 1-negotiating 2-transacted 3-terminated */
    private Integer status;

    /** 谈判人 / Negotiator */
    private String negotiatedBy;

    /** 谈判时间 / Negotiation time */
    private LocalDateTime negotiatedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
