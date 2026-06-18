-- ============================================
-- Atlas V102: 死信消息表 / Dead Message Table
-- 基于 09 报告 2.3.5，消息推送重试3次仍失败后移入此表 /
-- Based on 09 report 2.3.5, messages that fail after 3 retries are moved to this table
-- ============================================

CREATE TABLE IF NOT EXISTS dead_msg_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 / Primary key',
    original_msg_id BIGINT COMMENT '原始消息ID / Original message ID',
    supplier_id     BIGINT COMMENT '供应商ID / Supplier ID',
    user_id         BIGINT COMMENT '用户ID / User ID',
    title           VARCHAR(200) NOT NULL COMMENT '消息标题 / Message title',
    content         TEXT COMMENT '消息内容 / Message content',
    type            VARCHAR(50) NOT NULL COMMENT '消息类型 / Message type',
    channel         VARCHAR(20) NOT NULL COMMENT '推送通道: MAIL/SMS/WEBSOCKET / Push channel',
    recipient       VARCHAR(200) COMMENT '接收人地址（邮箱/手机号） / Recipient address (email/phone)',
    retry_count     INT DEFAULT 3 COMMENT '已重试次数 / Retry count',
    last_error      VARCHAR(2000) COMMENT '最后一次错误信息 / Last error message',
    failed_at       DATETIME COMMENT '最终失败时间 / Final failure time',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    INDEX idx_type (type),
    INDEX idx_channel (channel),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='死信消息记录表 / Dead message record table';
