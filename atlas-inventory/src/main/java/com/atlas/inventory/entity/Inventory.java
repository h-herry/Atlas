package com.atlas.inventory.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 库存实体 — 对应 atlas_inventory.inventory / Inventory entity — maps to atlas_inventory.inventory
 * <p>
 * 使用 version 字段实现乐观锁，配合 Seata AT 模式 undo_log 实现分布式事务。 /
 * Uses version field for optimistic locking, works with Seata AT mode undo_log for distributed transactions.
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Data
@TableName("inventory")
public class Inventory {

    /** 主键 / Primary key */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** SKU ID / SKU ID */
    private Long skuId;

    /** 仓库ID / Warehouse ID */
    private Long warehouseId;

    /** 当前库存 / Current quantity */
    private BigDecimal quantity;

    /** 已锁定（采购在途） / Locked (in-transit procurement) */
    private BigDecimal lockedQty;

    /** 安全库存阈值 / Safety stock threshold */
    private BigDecimal safetyStock;

    /** 乐观锁版本号 / Optimistic lock version */
    @Version
    private Integer version;

    /** 创建时间 / Creation time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Update time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
