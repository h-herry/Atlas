package com.atlas.receipt.settlement.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对账记录实体 / Reconciliation record entity
 *
 * @author Atlas Team
 * @since 1.2.502
 */
@Data
@TableName("reconciliation_record")
public class ReconciliationRecord {

    /** 记录ID（雪花算法） / Record ID (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联结算单ID / Related settlement ID */
    private Long settlementId;

    /** 企业端确认状态：true-确认 false-未确认 / Enterprise confirm: true-confirmed false-pending */
    private Boolean enterpriseConfirm;

    /** 供应商确认状态：true-确认 false-未确认 / Supplier confirm: true-confirmed false-pending */
    private Boolean supplierConfirm;

    /** 异议原因（供应商提出异议时填写） / Dispute reason (filled when supplier disputes) */
    private String disputeReason;

    /** 对账结果：MATCHED/MISMATCH/PENDING / Reconciliation result */
    private String status;

    /** 企业确认人 / Enterprise confirmed by */
    private Long enterpriseConfirmedBy;

    /** 企业确认时间 / Enterprise confirmed time */
    private LocalDateTime enterpriseConfirmedAt;

    /** 供应商确认人 / Supplier confirmed by */
    private Long supplierConfirmedBy;

    /** 供应商确认时间 / Supplier confirmed time */
    private LocalDateTime supplierConfirmedAt;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
