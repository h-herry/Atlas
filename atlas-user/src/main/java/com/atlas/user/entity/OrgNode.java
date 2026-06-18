package com.atlas.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 组织架构树形节点实体 — 支持集团→事业部→工厂→车间→产线 5级层级 /
 * Organization structure tree node entity — supports Group → BU → Plant → Workshop → Line 5-level hierarchy
 *
 * <p>供应商供货范围可关联到产线级，数据权限按组织节点隔离 /
 * Supplier supply scope can link to line level; data permission isolated by org node</p>
 *
 * @since 1.2.22
 */
@Data
@TableName("org_structure")
public class OrgNode {

    @TableId(type = IdType.ASSIGN_ID)
    private Long nodeId;

    private Long parentId;

    /** 节点类型: GROUP / DIVISION / PLANT / WORKSHOP / LINE */
    private String nodeType;

    private String name;
    private String code;

    /** 节点路径 (如 /1/2/5/) / Node path */
    private String nodePath;

    private Integer nodeLevel;
    private Integer sortOrder;
    private Long managerId;

    /** 状态: 1启用 0停用 / Status: 1-active 0-inactive */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
