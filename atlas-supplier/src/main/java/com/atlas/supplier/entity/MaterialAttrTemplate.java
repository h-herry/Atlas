package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物料规格属性模板实体 — 对应 material_attr_template 表 /
 * Material attribute template entity — maps to material_attr_template table
 *
 * <p>按物料分类定义结构化属性字段，支持 STRING / NUMBER / ENUM / DATE 四种类型，
 * 子分类自动继承父分类属性集，用于询价比价参数过滤。 /
 * Defines structured attribute fields per material category, supports STRING/NUMBER/ENUM/DATE types,
 * sub-categories auto-inherit parent attributes, used for RFQ price comparison parameter filtering.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("material_attr_template")
public class MaterialAttrTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联分类ID / Associated category ID */
    private Long categoryId;

    /** 属性名称 / Attribute name */
    private String attrName;

    /** 属性类型: STRING/NUMBER/ENUM/DATE / Attribute type */
    private String attrType;

    /** 是否必填: 0否 1是 / Required: 0=no, 1=yes */
    private Integer isRequired;

    /** 枚举值列表（逗号分隔，仅 ENUM 类型有效） / Enum values (comma-separated, ENUM type only) */
    private String enumValues;

    /** 单位 / Unit */
    private String unit;

    /** 排序序号 / Sort order */
    private Integer sortOrder;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
