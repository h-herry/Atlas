-- ============================================
-- Atlas V10: 合同电子签章 + 风险预警
-- 数据库: atlas_contract
-- ============================================

USE atlas_contract;

-- 扩展合同表：增加电子签章相关字段
ALTER TABLE contract
    ADD COLUMN IF NOT EXISTS sign_status TINYINT DEFAULT 0 COMMENT '0未签 1采购方已签 2供应商已签 3双方已签',
    ADD COLUMN IF NOT EXISTS signed_contract_url VARCHAR(500) COMMENT '已签合同文件URL';

-- 合同风险条款表
CREATE TABLE IF NOT EXISTS contract_risk_clause (
    id                      BIGINT PRIMARY KEY COMMENT '风险记录ID',
    contract_id             BIGINT NOT NULL COMMENT '合同ID',
    clause_content          TEXT COMMENT '风险条款原文',
    risk_type               VARCHAR(32) COMMENT '风险类型: PRICE价格风险/DELIVERY交付风险/PAYMENT付款风险/LEGAL法律风险',
    risk_level              VARCHAR(16) COMMENT '风险等级: HIGH/MEDIUM/LOW',
    suggestion              TEXT COMMENT '修改建议',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_contract (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同风险条款表';
