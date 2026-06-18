package com.atlas.receipt.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收货单主表实体 — 对应 atlas_receipt.receipt /
 * Receipt master entity — corresponds to atlas_receipt.receipt
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Data
@TableName("receipt")
public class Receipt {

    /** 收货单ID（雪花算法） / Receipt ID (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 收货单编号 / Receipt number */
    private String receiptNo;

    /** 关联采购订单ID / Related purchase order ID */
    private Long orderId;

    /** 收货仓库ID / Receiving warehouse ID */
    private Long warehouseId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 状态：0-待收货 1-部分收货 2-全部收货 3-质检中 4-已入库 / Status: 0-pending 1-partial 2-complete 3-inspecting 4-stored */
    private Integer status;

    /** 质检人 / Inspector */
    private Long inspectorId;

    /** 质检时间 / Inspection time */
    private LocalDateTime inspectedAt;

    /** 创建人 / Created by */
    private Long createdBy;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
