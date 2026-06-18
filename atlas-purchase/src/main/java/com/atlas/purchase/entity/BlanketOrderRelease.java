package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 一揽子订单分批释放记录实体 / Blanket order release record entity
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("blanket_order_release")
public class BlanketOrderRelease implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联一揽子订单ID / Associated blanket order ID */
    private Long blanketOrderId;

    /** 释放单号 / Release number */
    private String releaseNo;

    /** 本次释放数量 / Release quantity */
    private BigDecimal releaseQty;

    /** 本次释放金额 / Release amount */
    private BigDecimal releaseAmount;

    /** 释放日期 / Release date */
    private LocalDate releaseDate;

    /** 期望交货日 / Expected delivery date */
    private LocalDate expectedDeliveryDate;

    /** 状态：RELEASED/DELIVERED/CANCELLED / Status */
    private String status;

    /** 创建人 / Created by */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
