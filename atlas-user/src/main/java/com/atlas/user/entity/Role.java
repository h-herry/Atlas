package com.atlas.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 角色实体 / Role entity
 */
@Data
@TableName("role")
public class Role {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String roleCode;
    private String roleName;
    private Integer dataScope;
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
