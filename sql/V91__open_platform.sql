-- ============================================
-- Atlas-Open V1: 开放平台
-- 数据库: atlas_open
-- ============================================

CREATE DATABASE IF NOT EXISTS atlas_open DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE atlas_open;

-- 开放API客户端信息表
CREATE TABLE IF NOT EXISTS open_api_client (
    id                      BIGINT PRIMARY KEY COMMENT '客户端ID',
    client_name             VARCHAR(100) NOT NULL COMMENT '客户端名称',
    app_key                 VARCHAR(64) NOT NULL COMMENT '应用Key',
    app_secret              VARCHAR(128) NOT NULL COMMENT '应用密钥',
    access_apis             VARCHAR(500) COMMENT '授权API列表',
    rate_limit_per_min      INT DEFAULT 100 COMMENT '每分钟限流次数',
    status                  TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    expire_date             DATE COMMENT '过期日期',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_app_key (app_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='开放API客户端表';

-- Webhook订阅表
CREATE TABLE IF NOT EXISTS webhook_subscription (
    id                      BIGINT PRIMARY KEY COMMENT '订阅ID',
    client_id               BIGINT NOT NULL COMMENT '客户端ID',
    event_type              VARCHAR(32) COMMENT '事件类型: ORDER_CREATED/ORDER_STATUS_CHANGE/RECEIPT_CONFIRMED/INVOICE_UPLOADED',
    callback_url            VARCHAR(500) NOT NULL COMMENT '回调URL',
    secret                  VARCHAR(128) COMMENT '签名密钥',
    status                  TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_client (client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Webhook订阅表';

-- API调用日志表
CREATE TABLE IF NOT EXISTS api_call_log (
    id                      BIGINT PRIMARY KEY COMMENT '日志ID',
    client_id               BIGINT COMMENT '客户端ID',
    api_path                VARCHAR(200) COMMENT 'API路径',
    method                  VARCHAR(10) COMMENT 'HTTP方法',
    request_ip              VARCHAR(45) COMMENT '请求IP',
    response_status         INT COMMENT '响应状态码',
    duration_ms             BIGINT COMMENT '耗时(毫秒)',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_client_time (client_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用日志表';
