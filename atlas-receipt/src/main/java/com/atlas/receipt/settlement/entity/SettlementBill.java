package com.atlas.receipt.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 结算单实体 / Settlement bill entity
 *
 * @author Atlas Team
 * @since 1.2.502
 */
@Data
@TableName("settlement_bill")
public class SettlementBill {

    /** 结算单ID（雪花算法） / Settlement ID (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联收货单ID / Related receipt ID */
    private Long receiptId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 结算总金额 / Total settlement amount */
    private BigDecimal totalAmount;

    /** 状态：PENDING/APPROVED/RECONCILED/PAID */
    private String status;

    /** 创建人 / Created by */
    private Long createdBy;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
