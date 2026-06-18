-- ============================================
-- Atlas V22: 供应商配额管理 + 供应商整改跟踪
-- 数据库: atlas_supplier
-- ============================================

USE atlas_supplier;

-- 供应商配额表
CREATE TABLE IF NOT EXISTS supplier_quota (
    id                      BIGINT PRIMARY KEY COMMENT '配额ID',
    supplier_id             BIGINT NOT NULL COMMENT '供应商ID',
    material_category_id    BIGINT COMMENT '物料品类ID',
    quota_percent           DECIMAL(5,2) COMMENT '配额比例(%)',
    quota_status            TINYINT DEFAULT 1 COMMENT '1生效 0失效',
    effective_date          DATE COMMENT '生效日期',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_supplier_category (supplier_id, material_category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商配额表';

-- 供应商整改跟踪表
CREATE TABLE IF NOT EXISTS supplier_rectification (
    id                      BIGINT PRIMARY KEY COMMENT '整改单ID',
    supplier_id             BIGINT NOT NULL COMMENT '供应商ID',
    issue_type              VARCHAR(32) COMMENT '问题类型: QUALITY/DELIVERY/SERVICE/CERT',
    issue_desc              TEXT NOT NULL COMMENT '问题描述',
    severity                VARCHAR(16) COMMENT '严重程度: MAJOR/MINOR/CRITICAL',
    deadline                DATE COMMENT '整改截止日期',
    rectification_plan      TEXT COMMENT '供应商提交的整改方案',
    evidence_url            VARCHAR(500) COMMENT '整改证据附件',
    status                  TINYINT DEFAULT 0 COMMENT '0待整改 1方案已提交 2整改中 3待复评 4已完成 5逾期',
    auditor_id              BIGINT COMMENT '审核人ID',
    result                  VARCHAR(32) COMMENT '复评结果: PASS/FAIL/EXTEND',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_supplier_status (supplier_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商整改跟踪表';

-- 扩展 supplier_blacklist 表：增加风险源和风险等级字段
ALTER TABLE supplier_blacklist
    ADD COLUMN IF NOT EXISTS risk_source VARCHAR(32) COMMENT '风险来源: CREDIT征信/OPINION舆情/INTERNAL内部',
    ADD COLUMN IF NOT EXISTS risk_level VARCHAR(16) COMMENT '风险等级: HIGH/MEDIUM/LOW',
    ADD COLUMN IF NOT EXISTS monitor_expire_date DATE COMMENT '监控到期日';
