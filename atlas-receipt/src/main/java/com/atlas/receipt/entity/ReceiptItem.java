package com.atlas.receipt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收货明细表实体 — 对应 atlas_receipt.receipt_item /
 * Receipt item entity — corresponds to atlas_receipt.receipt_item
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Data
@TableName("receipt_item")
public class ReceiptItem {

    /** 明细ID（雪花算法） / Item ID (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联收货单ID / Related receipt ID */
    private Long receiptId;

    /** SKU ID */
    private Long skuId;

    /** 订单数量 / Ordered quantity */
    private BigDecimal orderQty;

    /** 实收数量 / Received quantity */
    private BigDecimal receivedQty;

    /** 合格数量 / Qualified quantity */
    private BigDecimal qualifiedQty;

    /** 不合格原因 / Rejection reason */
    private String rejectReason;
}
