-- 收货确认本地消息表，保障 MQ 发送失败时可定时补发
CREATE TABLE receipt_outbox (
    id BIGINT PRIMARY KEY,
    receipt_id BIGINT NOT NULL,
    message_topic VARCHAR(128) NOT NULL,
    message_tag VARCHAR(64),
    message_body TEXT NOT NULL,
    status TINYINT DEFAULT 0 COMMENT '0待发送 1已发送 2发送失败',
    retry_count INT DEFAULT 0,
    next_retry_time DATETIME,
    error_msg VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status_retry (status, next_retry_time)
);
