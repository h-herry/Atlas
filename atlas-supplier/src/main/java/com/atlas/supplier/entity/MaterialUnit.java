package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料多单位换算实体 / Material multi-unit conversion entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("material_unit")
public class MaterialUnit implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 物料ID（关联 goods.id） / Material ID (references goods.id) */
    private Long materialId;

    /** 单位名称 / Unit name */
    private String unitName;

    /** 换算率（相对于基本单位，如 1箱=24个 → conversionRate=24） / Conversion rate (relative to base unit) */
    private BigDecimal conversionRate;

    /** 使用场景: PURCHASE采购 / STOCK库存 / ISSUE发料 / Usage scenario */
    private String usageScene;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
