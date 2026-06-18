-- =============================================================================
-- V102__dead_msg.sql
-- 死信消息记录表 / Dead Message Record Table
-- 日期: 2026-06-18 / Date: 2026-06-18
-- =============================================================================

CREATE TABLE IF NOT EXISTS dead_msg_record (
    id            BIGINT        NOT NULL AUTO_INCREMENT                COMMENT '主键ID / Primary key',
    original_id   BIGINT                                                COMMENT '原始消息ID / Original message ID',
    user_id       BIGINT        NOT NULL                               COMMENT '目标用户ID / Target user ID',
    event_type    VARCHAR(32)   NOT NULL                               COMMENT '事件类型(SUPPLIER_REGISTER/CONTRACT_EXPIRE等) / Event type',
    title         VARCHAR(256)                                          COMMENT '消息标题 / Message title',
    content       TEXT                                                  COMMENT '消息内容 / Message content',
    push_channel  VARCHAR(16)                                           COMMENT '推送渠道(WEBSOCKET/MAIL/SMS) / Push channel',
    error_msg     VARCHAR(1024)                                         COMMENT '失败原因 / Failure reason',
    retry_count   INT           DEFAULT 0                              COMMENT '已重试次数 / Retry count',
    create_time   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP     COMMENT '创建时间 / Create time',
    PRIMARY KEY (id),
    INDEX idx_user_time (user_id, create_time),
    INDEX idx_event_type (event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='死信消息记录 / Dead Message Record';
