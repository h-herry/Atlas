-- ============================================
-- Atlas V16: 供应商风险监控增强
-- 数据库: atlas_supplier
-- 扩展 supplier_blacklist 表，新增风险来源与动态监控字段
-- ============================================

ALTER TABLE supplier_blacklist
    ADD COLUMN risk_source VARCHAR(32) COMMENT '风险来源: CREDIT/OPINION/INTERNAL' AFTER reason,
    ADD COLUMN risk_level VARCHAR(16) COMMENT '风险级别: HIGH/MEDIUM/LOW' AFTER risk_source,
    ADD COLUMN monitor_expire_date DATE COMMENT '监控过期日期' AFTER risk_level;
