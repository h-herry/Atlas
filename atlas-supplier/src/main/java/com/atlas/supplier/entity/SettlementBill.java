package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 财务结算单实体 — 对应 settlement_bill 表 / Financial settlement bill entity — maps to settlement_bill table
 *
 * <p>搭建统一的应付结算平台，支持按结算周期生成结算单。
 * 状态流程：0待对账 → 1供应商已确认 → 2采购方已确认 → 3已结算。 /
 * Unified payables settlement platform supporting period-based bill generation.
 * Status flow: 0=pending → 1=supplier confirmed → 2=buyer confirmed → 3=settled.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("settlement_bill")
public class SettlementBill {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 结算单号 / Settlement bill number */
    private String billNo;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 结算周期开始 / Period start */
    private LocalDate periodStart;

    /** 结算周期结束 / Period end */
    private LocalDate periodEnd;

    /** 结算金额 / Bill amount */
    private BigDecimal billAmount;

    /** 发票金额 / Invoice amount */
    private BigDecimal invoiceAmount;

    /** 发票号 / Invoice number */
    private String invoiceNo;

    /** 状态: 0待对账 1供应商已确认 2采购方已确认 3已结算 / Status: 0=pending, 1=supplier confirmed, 2=buyer confirmed, 3=settled */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
