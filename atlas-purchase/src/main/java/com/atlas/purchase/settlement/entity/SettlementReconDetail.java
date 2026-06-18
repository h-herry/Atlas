package com.atlas.purchase.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 对账单差异明细实体 — 逐项记录数量/金额差异 /
 * Reconciliation discrepancy detail entity — item-by-item quantity/amount discrepancy records
 *
 * @since 1.2.22
 */
@Data
@TableName("settlement_recon_detail")
public class SettlementReconDetail {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private Long reconId;

    /** 差异项类型: QTY_DIFF / PRICE_DIFF / MISSING_RECEIPT / MISSING_RETURN / OTHER */
    private String itemType;

    private String referenceNo;
    private BigDecimal systemQty;
    private BigDecimal supplierQty;
    private BigDecimal systemAmount;
    private BigDecimal supplierAmount;
    private BigDecimal diffAmount;

    /** 状态: OPEN / CONFIRMED / DISPUTED / RESOLVED */
    private String status;

    private String supplierFeedback;
    private Long resolvedBy;
    private LocalDateTime resolvedAt;
    private String note;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
