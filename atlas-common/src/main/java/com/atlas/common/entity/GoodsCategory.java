package com.atlas.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 商品分类实体 / Goods category entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("goods_category")
public class GoodsCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 父分类ID，0表示顶级分类 / Parent category ID, 0 = top-level */
    private Long parentId;

    /** 分类名称 / Category name */
    private String categoryName;

    /** 分类编码 / Category code */
    private String categoryCode;

    /** 排序序号 / Sort order */
    private Integer sortOrder;

    /** 状态：1启用 0停用 / Status: 1=active, 0=inactive */
    private Integer status;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
