package com.atlas.receipt.delivery.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * VMI库存实体 — 对应 vmi_inventory /
 * VMI inventory entity — maps to vmi_inventory
 * <p>
 * 供应商管理库存（VMI）模式下，物料存放在客户仓库，由供应商负责补货。 /
 * In Vendor Managed Inventory (VMI) mode, materials are stored in customer warehouses,
 * and suppliers are responsible for replenishment.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Data
@TableName("vmi_inventory")
public class VmiInventory {

    /** 主键 / Primary key */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 仓库编码 / Warehouse code */
    private String warehouseCode;

    /** 当前库存 / Current stock */
    private BigDecimal currentStock;

    /** 安全库存下限 / Min safety stock */
    private BigDecimal minSafetyStock;

    /** 最大库存上限 / Max stock */
    private BigDecimal maxStock;

    /** 补货点 / Replenish point */
    private BigDecimal replenishPoint;

    /** 在途量 / In-transit quantity */
    private BigDecimal inTransitQty;

    /** 已分配量 / Allocated quantity */
    private BigDecimal allocatedQty;

    /** 最后更新时间 / Last update time */
    private LocalDateTime lastUpdateTime;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
