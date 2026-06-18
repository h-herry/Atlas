package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * BOM 明细实体 / BOM line item entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("bom_item")
public class BomItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** BOM ID */
    private Long bomId;

    /** 物料ID（关联 goods.id） / Material ID (references goods.id) */
    private Long materialId;

    /** 用量 / Quantity per unit */
    private BigDecimal quantity;

    /** 单位 / Unit */
    private String unit;

    /** 损耗率(%) / Scrap rate (%) */
    private BigDecimal scrapRate;

    /** 是否关键物料: 0否 1是 / Key material: 0=no, 1=yes */
    private Integer isKeyItem;

    /** 替代物料ID / Substitute material ID */
    private Long substituteMaterialId;

    /** 备注 / Remark */
    private String remark;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
