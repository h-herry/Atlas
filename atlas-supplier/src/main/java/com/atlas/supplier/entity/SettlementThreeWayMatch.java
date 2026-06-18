package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 三单匹配实体 — 对应 settlement_three_way_match 表 / Three-way match entity — maps to settlement_three_way_match table
 *
 * <p>对采购订单金额、入库金额、发票金额进行三单自动匹配。
 * 匹配结果: MATCH(一致) / MISMATCH(差异)，差异详情写入 difference_desc。 /
 * Auto-matches purchase order amount, receipt amount, and invoice amount.
 * Match result: MATCH / MISMATCH; differences written to difference_desc.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("settlement_three_way_match")
public class SettlementThreeWayMatch {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 结算单ID / Settlement ID */
    private Long settlementId;

    /** 采购订单ID / Purchase order ID */
    private Long purchaseOrderId;

    /** 入库单ID / Receipt ID */
    private Long receiptId;

    /** 发票号 / Invoice number */
    private String invoiceNo;

    /** 订单金额 / Order amount */
    private BigDecimal orderAmount;

    /** 入库金额 / Receipt amount */
    private BigDecimal receiptAmount;

    /** 发票金额 / Invoice amount */
    private BigDecimal invoiceAmount;

    /** 匹配结果: MATCH一致 / MISMATCH差异 / Match result */
    private String matchResult;

    /** 差异说明 / Difference description */
    private String differenceDesc;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
