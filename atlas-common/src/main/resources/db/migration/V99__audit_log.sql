-- =============================================================================
-- V99__audit_log.sql
-- 审计日志表 / Audit Log Table
-- 记录所有带 @AuditLog 注解的业务操作日志，满足制造业合规四要素（who/when/what/result）
-- Records all business operations annotated with @AuditLog,
-- fulfilling manufacturing compliance 4-element requirement (who/when/what/result)
-- =============================================================================

CREATE TABLE IF NOT EXISTS `audit_log` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT            COMMENT '主键 / Primary key',
    `user_id`         BIGINT       DEFAULT NULL                        COMMENT '操作用户ID / Operator user ID',
    `username`        VARCHAR(64)  DEFAULT NULL                        COMMENT '操作用户名 / Operator username',
    `module`          VARCHAR(64)  NOT NULL                            COMMENT '业务模块: SUPPLIER/CONTRACT/PURCHASE/INVENTORY 等 / Business module',
    `operation`       VARCHAR(64)  NOT NULL                            COMMENT '操作类型: CREATE/UPDATE/DELETE/APPROVE 等 / Operation type',
    `description`     VARCHAR(256) DEFAULT NULL                        COMMENT '操作描述（中文摘要） / Operation description (brief Chinese)',
    `request_uri`     VARCHAR(512) DEFAULT NULL                        COMMENT '请求URI / Request URI',
    `request_method`  VARCHAR(10)  DEFAULT NULL                        COMMENT 'HTTP方法: GET/POST/PUT/DELETE / HTTP method',
    `request_params`  TEXT         DEFAULT NULL                        COMMENT '请求参数（JSON，最大2000字符） / Request params (JSON, max 2000 chars)',
    `response_status` VARCHAR(16)  NOT NULL DEFAULT 'SUCCESS'          COMMENT '执行结果: SUCCESS/FAILED / Execution result',
    `ip_address`      VARCHAR(64)  DEFAULT NULL                        COMMENT '客户端IP / Client IP address',
    `duration_ms`     BIGINT       DEFAULT 0                           COMMENT '执行耗时（毫秒） / Execution duration (ms)',
    `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间 / Creation time',
    PRIMARY KEY (`id`),
    KEY `idx_audit_user_id`     (`user_id`),
    KEY `idx_audit_module`      (`module`),
    KEY `idx_audit_created_at`  (`created_at`),
    KEY `idx_audit_module_op`   (`module`, `operation`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='审计日志表 / Audit log table';
