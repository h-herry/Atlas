package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 采购订单主表实体 / Purchase order main table entity
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Data
@TableName("purchase_order")
public class PurchaseOrder {

    /** 订单ID（雪花算法） / Order ID (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 订单编号 / Order number */
    private String orderNo;

    /** 关联合同ID / Associated contract ID */
    private Long contractId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 需求部门ID / Requesting department ID */
    private Long deptId;

    /** 采购标题 / Purchase title */
    private String title;

    /** 总金额 / Total amount */
    private BigDecimal totalAmount;

    /** 采购方式：1-公开招标 2-邀请招标 3-询比采购 4-竞价采购 5-竞争性谈判 6-竞争性磋商 7-单一来源 8-框架协议 9-合作创新 / Procurement type: 1-open bidding 2-invited bidding 3-inquiry 4-auction 5-negotiation 6-consultation 7-single source 8-framework 9-cooperative innovation */
    private Integer procurementType;

    /** 状态：0-草稿 1-待审批 2-已审批 3-执行中 4-已完成 5-已取消 / Status: 0-draft 1-pending approval 2-approved 3-executing 4-completed 5-cancelled */
    private Integer status;

    /** 请求幂等ID / Request idempotency ID */
    private String requestId;

    /** 审批人 / Approved by */
    private Long approvedBy;

    /** 审批时间 / Approval time */
    private LocalDateTime approvedAt;

    /** 创建人 / Created by */
    private Long createdBy;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
