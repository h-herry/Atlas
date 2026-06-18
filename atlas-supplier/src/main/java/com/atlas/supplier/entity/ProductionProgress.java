package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商生产进度报工实体 / Supplier production progress report entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("production_progress")
public class ProductionProgress implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 采购订单ID / Purchase order ID */
    private Long purchaseOrderId;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 订单总量 / Total order quantity */
    private BigDecimal totalQty;

    /** 已生产量 / Produced quantity */
    private BigDecimal producedQty;

    /** 进度百分比 / Progress percentage */
    private BigDecimal progressPercent;

    /** 预计完成日期 / Estimated completion date */
    private LocalDate estimatedCompletion;

    /** 填报时间 / Report time */
    private LocalDateTime reportTime;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
