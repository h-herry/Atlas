package com.atlas.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品主表实体（SKU） / Goods master entity (SKU)
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("goods")
public class Goods implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 商品名称 / Goods name */
    private String goodsName;

    /** 商品编码（SKU编码） / Goods code (SKU code) */
    private String goodsCode;

    /** 所属分类ID / Category ID */
    private Long categoryId;

    /** 规格型号 / Specification */
    private String spec;

    /** 单位 / Unit */
    private String unit;

    /** 品牌 / Brand */
    private String brand;

    /** 默认价格 / Default price */
    private BigDecimal defaultPrice;

    /** 状态：1启用 0停用 / Status: 1=active, 0=inactive */
    private Integer status;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
