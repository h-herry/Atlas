package com.atlas.receipt.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 结算明细实体 / Settlement item entity
 *
 * @author Atlas Team
 * @since 1.2.401
 */
@Data
@TableName("settlement_item")
public class SettlementItem {

    /** 明细ID（雪花算法） / Item ID (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联结算单ID / Related settlement ID */
    private Long settlementId;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 结算数量 / Settlement quantity */
    private BigDecimal quantity;

    /** 结算单价 / Settlement unit price */
    private BigDecimal unitPrice;

    /** 结算金额 / Settlement amount */
    private BigDecimal amount;
}
