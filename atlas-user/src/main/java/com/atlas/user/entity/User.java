package com.atlas.user.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体 — 对应 atlas_user 库 user 表 /
 * User entity — corresponds to atlas_user.user table
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String username;
    private String realName;
    private String passwordHash;
    private String email;
    private String phone;
    private Long deptId;
    private String position;
    private Integer status;

    private LocalDateTime lastLoginAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
