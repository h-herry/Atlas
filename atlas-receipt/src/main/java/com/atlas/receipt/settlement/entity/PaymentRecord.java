package com.atlas.receipt.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 付款记录实体 / Payment record entity
 *
 * @author Atlas Team
 * @since 1.2.401
 */
@Data
@TableName("payment_record")
public class PaymentRecord {

    /** 付款ID（雪花算法） / Payment ID (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联结算单ID / Related settlement ID */
    private Long settlementId;

    /** 付款金额 / Payment amount */
    private BigDecimal payAmount;

    /** 付款方式：BANK_TRANSFER/CHECK/CASH/ONLINE / Payment method */
    private String payMethod;

    /** 付款时间 / Payment time */
    private LocalDateTime payTime;

    /** 付款状态：PENDING/APPROVED/PAID/CANCELLED / Payment status */
    private String status;

    /** 付款人 / Paid by */
    private Long paidBy;

    /** 审批人 / Approved by */
    private Long approvedBy;

    /** 审批时间 / Approved time */
    private LocalDateTime approvedAt;

    /** 备注 / Remark */
    private String remark;

    /** 创建人 / Created by */
    private Long createdBy;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
