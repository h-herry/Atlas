package com.atlas.common.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 角色权限关联实体 / Role-Permission Relation Entity
 * <p>
 * 多对多关联表，记录角色拥有的权限 / Many-to-many join table recording role-permission assignments
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("sys_role_permission")
public class SysRolePermission {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 角色ID / Role ID */
    private Long roleId;

    /** 权限ID / Permission ID */
    private Long permissionId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
