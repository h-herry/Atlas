-- ============================================
-- Atlas V8: 供应商主表增强（SRM 扩展字段）
-- ============================================

ALTER TABLE supplier
    MODIFY COLUMN grade VARCHAR(4) DEFAULT 'C' COMMENT '供应商等级: A/B/C/D',
    ADD COLUMN lifecycle_status TINYINT DEFAULT 1 COMMENT '生命周期状态: 1潜在 2准入中 3合格 4合作中 5冻结 6淘汰 7黑名单',
    ADD COLUMN rating_score DECIMAL(5,2) DEFAULT 0 COMMENT '综合评分(满分100)',
    ADD COLUMN cooperation_start_date DATE COMMENT '首次合作日期',
    ADD COLUMN cooperation_years INT DEFAULT 0 COMMENT '合作年限',
    ADD COLUMN annual_purchase_amount DECIMAL(14,2) DEFAULT 0 COMMENT '年度采购额',
    ADD COLUMN main_categories TEXT COMMENT '主营品类(JSON数组)',
    ADD COLUMN bank_name VARCHAR(100) COMMENT '开户行',
    ADD COLUMN bank_account VARCHAR(50) COMMENT '银行账号',
    ADD COLUMN tax_rate DECIMAL(5,2) COMMENT '税率(%)',
    ADD COLUMN payment_terms VARCHAR(32) DEFAULT 'NET30' COMMENT '付款条件: NET30/NET60/NET90',
    ADD COLUMN is_qualified TINYINT DEFAULT 1 COMMENT '是否合格供应商: 0否 1是',
    ADD COLUMN risk_level VARCHAR(16) DEFAULT 'LOW' COMMENT '风险等级: LOW/MEDIUM/HIGH/CRITICAL',
    ADD INDEX idx_lifecycle (lifecycle_status),
    ADD INDEX idx_grade (grade),
    ADD INDEX idx_risk_level (risk_level);

-- 模拟 Flyway 已执行记录
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES (8, '8', 'supplier enhance', 'SQL', 'V8__supplier_enhance.sql', 0, 'atlas', NOW(), 0, 1);
