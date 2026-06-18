package com.atlas.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存变动流水实体 — 对应 atlas_inventory.inventory_log /
 * Inventory change log entity — maps to atlas_inventory.inventory_log
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Data
@TableName("inventory_log")
public class InventoryChangeLog {

    /** 主键 / Primary key */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** SKU ID / SKU ID */
    private Long skuId;

    /** 仓库ID / Warehouse ID */
    private Long warehouseId;

    /** 变动类型：1-采购入库 2-销售出库 3-退货入库 4-盘点调整 /
     *  Change type: 1-purchase inbound 2-sale outbound 3-return inbound 4-inventory check */
    private Integer changeType;

    /** 变动数量（正增负减） / Change quantity (positive=add, negative=deduct) */
    private BigDecimal changeQty;

    /** 变动前库存 / Quantity before change */
    private BigDecimal beforeQty;

    /** 变动后库存 / Quantity after change */
    private BigDecimal afterQty;

    /** 关联订单号 / Associated order number */
    private String orderNo;

    /** 创建时间 / Creation time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
