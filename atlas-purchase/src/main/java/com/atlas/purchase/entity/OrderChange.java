package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 订单变更管理（ECN）实体 / Order change management (ECN) entity
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("order_change")
public class OrderChange implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 变更单号 / Change order number */
    private String changeNo;

    /** 关联采购订单ID / Associated purchase order ID */
    private Long orderId;

    /** 变更类型：QTY_CHANGE/PRICE_CHANGE/DATE_CHANGE/ITEM_ADD/ITEM_REMOVE / Change type */
    private String changeType;

    /** 变更原因 / Change reason */
    private String changeReason;

    /** 状态：DRAFT/PENDING_APPROVE/APPROVED/SUPPLIER_CONFIRMED/EXECUTED/REJECTED / Status */
    private String status;

    /** 审批人ID / Approved by */
    private Long approvedBy;

    /** 审批时间 / Approval time */
    private LocalDateTime approvedAt;

    /** 供应商确认时间 / Supplier confirmed time */
    private LocalDateTime supplierConfirmedAt;

    /** 创建人 / Created by */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
