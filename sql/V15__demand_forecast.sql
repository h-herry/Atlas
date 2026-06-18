-- ============================================
-- Atlas V15: 需求预测协同
-- 数据库: atlas_supplier
-- ============================================

-- 需求预测表
CREATE TABLE demand_forecast (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    forecast_no VARCHAR(64) NOT NULL COMMENT '预测编号',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    forecast_qty DECIMAL(14,2) COMMENT '预测数量',
    forecast_month VARCHAR(7) COMMENT '预测月份 YYYY-MM',
    confidence_level VARCHAR(16) COMMENT '置信度: HIGH/MEDIUM/LOW',
    source VARCHAR(32) COMMENT '来源: SALES/PLAN/HISTORY',
    shared_to_supplier TINYINT DEFAULT 0 COMMENT '是否已分享给供应商',
    supplier_feedback_qty DECIMAL(14,2) COMMENT '供应商承诺量',
    supplier_feedback_date DATE COMMENT '供应商反馈日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_forecast_no (forecast_no)
) COMMENT='需求预测协同';
