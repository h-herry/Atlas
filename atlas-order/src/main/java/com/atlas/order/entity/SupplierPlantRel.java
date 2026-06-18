package com.atlas.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商-工厂关联实体 — 对应 supplier_plant_rel /
 * Supplier-Plant relation entity — maps to supplier_plant_rel
 * <p>
 * 定义供应商可供应哪些工厂，以及对应的优先级、提前期和月度产能上限。 /
 * Defines which plants a supplier can serve, with priority, lead time, and monthly capacity cap.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Data
@TableName("supplier_plant_rel")
public class SupplierPlantRel {

    /** 主键 / Primary key */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 工厂编码 / Plant code */
    private String plantCode;

    /** 优先级(0-最高,越大越低) / Priority (0-highest, larger=lower) */
    private Integer priority;

    /** 供货提前期(天) / Lead time (days) */
    private Integer leadTimeDays;

    /** 月度产能上限 / Monthly capacity cap */
    private BigDecimal capacityPerMonth;

    /** 是否启用: 0-禁用 1-启用 / Whether active: 0-disabled 1-active */
    private Integer isActive;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
