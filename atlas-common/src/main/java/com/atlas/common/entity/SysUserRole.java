package com.atlas.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 用户角色关联实体 / User-Role Relation Entity
 * <p>
 * 支持按组织节点隔离的数据权限：org_node_id 指向 org_structure.node_id /
 * Supports org-node-based data isolation: org_node_id references org_structure.node_id
 * <p>
 * org_node_id 为 NULL 表示该角色在全局生效 / org_node_id NULL means the role is globally effective
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("sys_user_role")
public class SysUserRole {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID / User ID */
    private Long userId;

    /** 角色ID / Role ID */
    private Long roleId;

    /** 数据隔离节点ID(关联org_structure, NULL表示全局) / Org node ID for data isolation (NULL = global) */
    private Long orgNodeId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
