package com.atlas.user.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户凭据实体 — 存储加密密码、通道标识、锁定状态 /
 * User credential entity — stores encrypted password, channel identifier, lock status
 *
 * <pre>
 * CREATE TABLE user_credential (
 *     id          BIGINT       PRIMARY KEY COMMENT '主键',
 *     user_id     BIGINT       NOT NULL UNIQUE COMMENT '关联 user.id',
 *     password_hash VARCHAR(255) NOT NULL COMMENT 'BCrypt 密码哈希',
 *     channel     VARCHAR(20)  NOT NULL DEFAULT 'enterprise' COMMENT '通道: enterprise|supplier',
 *     status      TINYINT      NOT NULL DEFAULT 1 COMMENT '状态: 1-正常 0-禁用',
 *     locked_until DATETIME    NULL     COMMENT '锁定截止时间，NULL 表示未锁定',
 *     created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 *     updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
 *     INDEX idx_user_id (user_id)
 * ) COMMENT '用户凭据表';
 * </pre>
 */
@Data
@TableName("user_credential")
public class UserCredential {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联 user 表主键 / References user.id */
    private Long userId;

    /** BCrypt 加密后的密码哈希 / BCrypt-hashed password */
    private String passwordHash;

    /** 通道标识：enterprise（企业端） / supplier（供应商端） / Channel identifier */
    private String channel;

    /** 状态：1-正常 0-禁用 / Status: 1-active 0-disabled */
    private Integer status;

    /** 锁定截止时间，NULL 表示未锁定 / Lock expiration, NULL = not locked */
    private LocalDateTime lockedUntil;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
