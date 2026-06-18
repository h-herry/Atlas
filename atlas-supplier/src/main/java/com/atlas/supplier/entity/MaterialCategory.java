package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 物料分类实体（4级树形结构） — 对应 material_category 表 /
 * Material category entity (4-level tree structure) — maps to material_category table
 *
 * <p>层级：大类(1) → 中类(2) → 小类(3) → 细类(4)，支持 UNSPSC 标准分类码映射。 /
 * Hierarchy: Category(1) → Sub-category(2) → Class(3) → Sub-class(4), with UNSPSC code mapping support.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("material_category")
public class MaterialCategory implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 父分类ID，0表示顶级分类 / Parent category ID, 0 = top-level */
    private Long parentId;

    /** 分类编码 / Category code */
    private String code;

    /** 分类名称 / Category name */
    private String name;

    /** 层级: 1大类 2中类 3小类 4细类 / Level: 1-category, 2-subcategory, 3-class, 4-subclass */
    private Integer level;

    /** UNSPSC 标准分类码映射 / UNSPSC standard classification code mapping */
    private String standardCode;

    /** 排序序号 / Sort order */
    private Integer sortOrder;

    /** 状态: 1启用 0停用 / Status: 1=active, 0=inactive */
    private Integer status;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
