package com.atlas.purchase.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商对账单实体 — 周期内所有交付+结算汇总 /
 * Supplier reconciliation entity — aggregation of all deliveries and settlements within a period
 *
 * <p>支持供应商端确认/异议反馈，差异明细记录在 settlement_recon_detail 表 /
 * Supports supplier confirmation/dispute feedback; discrepancy details in settlement_recon_detail table</p>
 *
 * @since 1.2.22
 */
@Data
@TableName("settlement_recon")
public class SettlementRecon {

    @TableId(type = IdType.ASSIGN_ID)
    private Long reconId;

    private String reconNo;
    private Long supplierId;
    private String supplierName;
    private LocalDate periodStart;
    private LocalDate periodEnd;

    /** 系统对账金额 / System reconciliation amount */
    private BigDecimal reconAmount;

    /** 供应商确认金额 / Supplier confirmed amount */
    private BigDecimal confirmedAmount;

    /** 差异总金额 / Total difference amount */
    private BigDecimal diffAmount;

    /** 状态: DRAFT / SENT / SUPPLIER_PENDING / DISPUTED / CONFIRMED / RESOLVED */
    private String status;

    private LocalDateTime sentAt;
    private LocalDateTime supplierConfirmedAt;
    private LocalDateTime resolvedAt;
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
