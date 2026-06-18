package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 交期承诺与预警实体 / Delivery commitment & alert entity
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("delivery_commitment")
public class DeliveryCommitment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联订单ID / Associated order ID */
    private Long orderId;

    /** 订单行号 / Order line number */
    private Integer lineNo;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 需求交期 / Requested delivery date */
    private LocalDate requestedDate;

    /** 供应商承诺交期 / Supplier committed date */
    private LocalDate committedDate;

    /** 实际确认交期 / Confirmed delivery date */
    private LocalDate confirmedDate;

    /** 实际到货日 / Actual delivery date */
    private LocalDate actualDeliveryDate;

    /** 偏差天数（正数为延迟） / Deviation days (positive means delay) */
    private Integer deviationDays;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 是否已预警 / Whether alerted */
    private Integer alerted;

    /** 预警发送时间 / Alert sent time */
    private LocalDateTime alertSentAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
