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
 * MRP 计算结果实体 / MRP calculation result entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("mrp_result")
public class MrpResult implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** MRP计划ID / MRP plan ID */
    private Long planId;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 毛需求 / Gross demand */
    private BigDecimal grossDemand;

    /** 当前库存 / Current stock */
    private BigDecimal currentStock;

    /** 在途量 / In-transit quantity */
    private BigDecimal inTransit;

    /** 净需求（= 毛需求 - 当前库存 - 在途量） / Net demand (= gross demand - current stock - in-transit) */
    private BigDecimal netDemand;

    /** 建议采购量 / Planned order quantity */
    private BigDecimal plannedOrderQty;

    /** 建议开始日期 / Planned start date */
    private LocalDate plannedStartDate;

    /** 建议到货日期 / Planned end date */
    private LocalDate plannedEndDate;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
