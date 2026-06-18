-- ============================================
-- Atlas V9: 供应商资质表增强（SRM 扩展字段）
-- ============================================

ALTER TABLE supplier_qualification
    ADD COLUMN cert_type VARCHAR(32) COMMENT '证书类型: BUSINESS_LICENSE/ISO9001/ISO14001/OHSAS18001/CCC/ROHS/INDUSTRY_PERMIT',
    ADD COLUMN cert_authority VARCHAR(100) COMMENT '发证机关',
    ADD COLUMN cert_file_url VARCHAR(500) COMMENT '证书文件URL',
    ADD COLUMN verify_status TINYINT DEFAULT 0 COMMENT '验证状态: 0未验证 1验证通过 2验证失败 3过期',
    ADD COLUMN verify_platform VARCHAR(50) COMMENT '验证平台: TIANYANCHA/QICHACHA/CREDIT_CHINA',
    ADD COLUMN verify_result TEXT COMMENT '验证结果详情(JSON)',
    ADD COLUMN verified_at DATETIME COMMENT '验证时间',
    ADD COLUMN alert_before_days INT DEFAULT 30 COMMENT '到期预警天数',
    ADD INDEX idx_verify_status (verify_status);
-- 注意: idx_expire_date 已在 V2 中创建，此处不再重复

-- 模拟 Flyway 已执行记录
INSERT INTO flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success)
VALUES (9, '9', 'supplier qualification enhance', 'SQL', 'V9__supplier_qualification_enhance.sql', 0, 'atlas', NOW(), 0, 1);
