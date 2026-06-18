package com.atlas.purchase.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 三单匹配实体 — PO行项 ↔ 收货记录 ↔ 发票行项按物料+数量+单价三维校验 /
 * Three-way match entity — PO line ↔ receipt record ↔ invoice line matched by material + quantity + unit price
 *
 * <p>匹配成功后将关联结算状态更新为 READY /
 * Upon successful match, updates settlement status to READY</p>
 *
 * @since 1.2.22
 */
@Data
@TableName("three_way_match")
public class ThreeWayMatch {

    @TableId(type = IdType.ASSIGN_ID)
    private Long matchId;

    private Long poId;
    private Long poLineId;
    private Long receiveId;
    private Long receiveLineId;
    private Long invoiceId;
    private Long invoiceLineId;
    private Long materialId;
    private String materialCode;

    /** 订单数量 / PO quantity */
    private BigDecimal poQty;

    /** 收货数量 / Received quantity */
    private BigDecimal receiveQty;

    /** 发票数量 / Invoice quantity */
    private BigDecimal invoiceQty;

    /** 订单单价 / PO unit price */
    private BigDecimal poUnitPrice;

    /** 发票单价 / Invoice unit price */
    private BigDecimal invoiceUnitPrice;

    /** 匹配状态: UNMATCHED / MATCHED / PARTIAL / DISCREPANCY */
    private String matchStatus;

    private BigDecimal qtyDiscrepancy;
    private BigDecimal priceDiscrepancy;
    private BigDecimal toleranceQtyPct;
    private BigDecimal tolerancePricePct;

    /** 差异处理方式: WAIT_MANUAL / CONCESSION / ADJUST / REJECTED */
    private String resolution;

    private Long resolutionBy;
    private LocalDateTime resolutionAt;
    private String resolutionNote;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
