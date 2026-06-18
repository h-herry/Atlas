package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商配额实体 — 对应 supplier_quota 表 / Supplier quota entity — maps to supplier_quota table
 *
 * <p>支持按物料品类配置供应商配额比例，实现差异化配额管理。
 * 配额状态 1-生效 / 0-失效，失效配额不参与分配。 /
 * Supports configuring supplier quota percentages by material category for differentiated quota management.
 * Quota status: 1=active, 0=inactive; inactive quotas are excluded from allocation.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("supplier_quota")
public class SupplierQuota {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 物料品类ID / Material category ID */
    private Long materialCategoryId;

    /** 配额比例(%) / Quota percent (%) */
    private BigDecimal quotaPercent;

    /** 配额状态: 1生效 0失效 / Quota status: 1=active, 0=inactive */
    private Integer quotaStatus;

    /** 生效日期 / Effective date */
    private LocalDate effectiveDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
