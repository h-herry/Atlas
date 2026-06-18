package com.atlas.common.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 组织架构树形实体 / Organization Structure Tree Entity
 * <p>
 * 支持多级组织架构：GROUP(集团) → DIVISION(事业部) → PLANT(工厂) → WORKSHOP(车间) → LINE(产线) /
 * Supports multi-level org structure: GROUP → DIVISION → PLANT → WORKSHOP → LINE
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("org_structure")
public class OrgStructure {

    @TableId(type = IdType.INPUT)
    private Long nodeId;

    /** 父节点ID / Parent node ID */
    private Long parentId;

    /** 节点类型: GROUP/DIVISION/PLANT/WORKSHOP/LINE / Node type */
    private String nodeType;

    /** 节点名称 / Node name */
    private String name;

    /** 节点编码 / Node code */
    private String code;

    /** 节点路径(如 /1/2/5/) / Node path (e.g., /1/2/5/) */
    private String nodePath;

    /** 节点层级 / Node level */
    private Integer nodeLevel;

    /** 排序号 / Sort order */
    private Integer sortOrder;

    /** 负责人ID / Manager ID */
    private Long managerId;

    /** 状态(1启用0停用) / Status (1-active 0-inactive) */
    private Integer status;
}
