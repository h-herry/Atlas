-- ====================================================================
-- Atlas 物料管理模块 — V21: 供应商绩效汇总 + 物料分析报表
-- 日期：2026-06-17
-- ====================================================================

-- 供应商绩效汇总表
CREATE TABLE IF NOT EXISTS supplier_performance (
    id BIGINT PRIMARY KEY,
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    period VARCHAR(32) COMMENT '统计周期: 2026-Q2',
    delivery_on_time_rate DECIMAL(5,2) COMMENT '交付准时率(%)',
    quality_pass_rate DECIMAL(5,2) COMMENT '质量合格率(%)',
    avg_response_hours DECIMAL(8,2) COMMENT '平均响应时长(小时)',
    complaint_count INT DEFAULT 0 COMMENT '投诉次数',
    score DECIMAL(5,2) COMMENT '综合得分',
    grade VARCHAR(8) COMMENT '评级: A/B/C/D',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_supplier_period (supplier_id, period),
    UNIQUE KEY uk_supplier_period (supplier_id, period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商绩效汇总表';

-- 物料分析报表表
CREATE TABLE IF NOT EXISTS material_analysis (
    id BIGINT PRIMARY KEY,
    material_id BIGINT NOT NULL COMMENT '物料ID',
    period VARCHAR(32) COMMENT '统计周期',
    stock_turnover_rate DECIMAL(8,2) COMMENT '库存周转率',
    avg_stock_days DECIMAL(5,1) COMMENT '平均库存天数',
    slow_moving_qty DECIMAL(14,2) COMMENT '呆滞料数量',
    purchase_cost_avg DECIMAL(14,2) COMMENT '采购均价',
    purchase_cost_trend VARCHAR(32) COMMENT '成本趋势: RISE/FALL/STABLE',
    demand_forecast DECIMAL(14,2) COMMENT '需求预测',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_material_period (material_id, period),
    UNIQUE KEY uk_material_period (material_id, period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料分析报表表';
