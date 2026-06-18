-- ===================================================
-- V92: 第三方 API 可配置对接 — 配置表 + 接口定义 + 调用日志
-- ===================================================

-- 第三方 API 配置表
CREATE TABLE third_party_api_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_code VARCHAR(64) NOT NULL COMMENT '配置编码',
    config_name VARCHAR(100) NOT NULL COMMENT '配置名称',
    platform_name VARCHAR(64) COMMENT '第三方平台名称: SAP/Oracle/用友/金蝶/WMS/自定义',
    base_url VARCHAR(200) NOT NULL COMMENT 'API基础URL',
    auth_type VARCHAR(32) DEFAULT 'NONE' COMMENT '鉴权方式: NONE/BASIC/BEARER/API_KEY/OAUTH2/HMAC',
    auth_config TEXT COMMENT '鉴权配置JSON: {"username":"","password":""} 或 {"token_url":"","client_id":"","client_secret":""}',
    headers TEXT COMMENT '默认请求头JSON',
    timeout_ms INT DEFAULT 10000,
    retry_count INT DEFAULT 3,
    status TINYINT DEFAULT 1 COMMENT '1启用 0禁用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_config_code (config_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='第三方API配置';

-- API接口定义表
CREATE TABLE api_endpoint_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_id BIGINT NOT NULL,
    endpoint_code VARCHAR(64) NOT NULL COMMENT '接口编码',
    endpoint_name VARCHAR(100) COMMENT '接口名称',
    method VARCHAR(10) DEFAULT 'POST' COMMENT 'GET/POST/PUT/DELETE',
    path VARCHAR(200) NOT NULL COMMENT '接口路径',
    request_template TEXT COMMENT '请求模板(JSON)，支持变量占位符 {{xxx}}',
    response_mapping TEXT COMMENT '响应映射JSON: {"target_field":"$.source_path"}',
    success_condition VARCHAR(200) COMMENT '成功判断: $.code==200',
    description VARCHAR(200),
    status TINYINT DEFAULT 1,
    INDEX idx_config (config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API接口定义';

-- API调用日志
CREATE TABLE api_integration_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_id BIGINT,
    endpoint_code VARCHAR(64),
    request_url VARCHAR(500),
    request_body MEDIUMTEXT,
    response_body MEDIUMTEXT,
    response_status INT,
    duration_ms BIGINT,
    success TINYINT DEFAULT 1,
    error_msg VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_config_time (config_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='API调用日志';
