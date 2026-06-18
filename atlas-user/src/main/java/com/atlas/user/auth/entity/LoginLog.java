package com.atlas.user.auth.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 登录日志实体 — 记录每次登录行为 /
 * Login log entity — records every login attempt
 *
 * <pre>
 * CREATE TABLE login_log (
 *     id           BIGINT       PRIMARY KEY COMMENT '主键',
 *     user_id      BIGINT       NOT NULL COMMENT '用户 ID',
 *     login_time   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
 *     ip           VARCHAR(45)  NULL     COMMENT '客户端 IP',
 *     result       TINYINT      NOT NULL COMMENT '结果: 1-成功 0-失败',
 *     fail_reason  VARCHAR(255) NULL     COMMENT '失败原因（成功时为 NULL）',
 *     created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
 *     INDEX idx_user_id (user_id),
 *     INDEX idx_login_time (login_time)
 * ) COMMENT '登录日志表';
 * </pre>
 */
@Data
@TableName("login_log")
public class LoginLog {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 用户 ID / User ID */
    private Long userId;

    /** 登录时间 / Login time */
    private LocalDateTime loginTime;

    /** 客户端 IP 地址 / Client IP address */
    private String ip;

    /** 登录结果：1-成功 0-失败 / Login result: 1-success 0-failure */
    private Integer result;

    /** 失败原因，成功时为 NULL / Failure reason, NULL on success */
    private String failReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
