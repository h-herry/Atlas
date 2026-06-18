package com.atlas.material.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料主数据实体 / Material master data entity
 * <p>
 * 用于 MOQ/EOQ/订货倍数校验的物料基础信息。 /
 * Used for MOQ/EOQ/order multiple validation.
 * </p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("material")
public class Material {

    /** 物料ID（雪花算法） / Material ID (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 物料编码 / Material code */
    private String materialCode;

    /** 物料名称 / Material name */
    private String materialName;

    /** 最小起订量（MOQ） / Minimum order quantity */
    private BigDecimal minOrderQty;

    /** 经济订货量（EOQ） / Economic order quantity */
    private BigDecimal economicOrderQty;

    /** 订货量倍数（订货量必须是此值的整数倍） / Order quantity multiple (qty must be integer multiple of this value) */
    private BigDecimal orderQtyMultiple;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
