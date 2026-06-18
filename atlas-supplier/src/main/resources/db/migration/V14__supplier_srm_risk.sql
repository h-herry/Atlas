-- ============================================
-- Atlas V14: 供应商风险管理 (SRM Risk)
-- ============================================

-- 10. 风险事件
CREATE TABLE risk_event (
    id BIGINT PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    risk_type VARCHAR(32) NOT NULL COMMENT '类型: LEGAL/FINANCIAL/QUALITY/DELIVERY/CREDIT',
    risk_level VARCHAR(16) NOT NULL COMMENT '等级: LOW/MEDIUM/HIGH/CRITICAL',
    event_desc VARCHAR(500) NOT NULL COMMENT '事件描述',
    event_source VARCHAR(50) COMMENT '来源: EXTERNAL_API/INSPECTION/COMPLAINT',
    occur_time DATETIME COMMENT '发生时间',
    status TINYINT DEFAULT 0 COMMENT '0待处理 1处理中 2已解决 3已关闭',
    handle_result VARCHAR(500) COMMENT '处理结果',
    handler_id BIGINT,
    handler_name VARCHAR(50),
    handled_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_supplier_risk (supplier_id, risk_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='风险事件';

-- 11. 预警规则
CREATE TABLE alert_rule (
    id BIGINT PRIMARY KEY,
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(32) NOT NULL COMMENT '类型: QUALIFICATION_EXPIRE/DELIVERY_DELAY/QUALITY_DEFECT/SCORE_FALL',
    trigger_condition TEXT NOT NULL COMMENT '触发条件（JSON）',
    alert_level VARCHAR(16) DEFAULT 'MEDIUM',
    notify_users TEXT COMMENT '通知人员ID（JSON数组）',
    is_enabled TINYINT DEFAULT 1,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警规则';

-- 12. 预警记录
CREATE TABLE alert_record (
    id BIGINT PRIMARY KEY,
    rule_id BIGINT COMMENT '关联规则ID',
    supplier_id BIGINT NOT NULL,
    alert_type VARCHAR(32) NOT NULL,
    alert_level VARCHAR(16) NOT NULL,
    alert_title VARCHAR(200) NOT NULL,
    alert_content TEXT COMMENT '预警详情',
    is_read TINYINT DEFAULT 0 COMMENT '0未读 1已读',
    is_handled TINYINT DEFAULT 0 COMMENT '0未处理 1已处理',
    handler_id BIGINT,
    handle_time DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_supplier_alert (supplier_id, is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预警记录';

-- 13. 黑名单
CREATE TABLE supplier_blacklist (
    id BIGINT PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    supplier_name VARCHAR(100),
    black_reason TEXT NOT NULL COMMENT '拉黑原因',
    black_type VARCHAR(32) DEFAULT 'PERMANENT' COMMENT 'PERMANENT永久/TEMPORARY临时',
    effective_date DATE NOT NULL COMMENT '生效日期',
    expire_date DATE COMMENT '过期日期（临时拉黑用）',
    operator_id BIGINT,
    operator_name VARCHAR(50),
    status TINYINT DEFAULT 1 COMMENT '1生效 0解除',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_supplier_black (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商黑名单';
