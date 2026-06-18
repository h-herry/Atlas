-- ============================================
-- Atlas V13: 供应商协同 (SRM Collaboration)
-- ============================================

-- 7. 预测计划
CREATE TABLE forecast_notice (
    id BIGINT PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    forecast_period VARCHAR(20) NOT NULL COMMENT '预测周期: 2026-06',
    material_name VARCHAR(100) COMMENT '物料名称',
    forecast_qty INT COMMENT '预测数量',
    unit VARCHAR(20) COMMENT '单位',
    confidence_level VARCHAR(20) COMMENT '置信度: HIGH/MEDIUM/LOW',
    remark VARCHAR(500),
    publisher_id BIGINT,
    publisher_name VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_supplier_period (supplier_id, forecast_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预测计划';

-- 8. 发货单
CREATE TABLE delivery_order (
    id BIGINT PRIMARY KEY,
    delivery_no VARCHAR(64) NOT NULL COMMENT '发货单号',
    purchase_order_id BIGINT COMMENT '关联采购单ID',
    supplier_id BIGINT NOT NULL,
    supplier_name VARCHAR(100),
    logistics_company VARCHAR(100) COMMENT '物流公司',
    tracking_no VARCHAR(100) COMMENT '物流单号',
    estimated_arrive_date DATE COMMENT '预计到达日期',
    actual_arrive_date DATE COMMENT '实际到达日期',
    delivery_items TEXT COMMENT '发货明细（JSON: [{materialId, qty}]）',
    status TINYINT DEFAULT 0 COMMENT '0待发货 1运输中 2已到货 3已签收 4部分退货',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_delivery_no (delivery_no),
    INDEX idx_supplier_id (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='发货单';

-- 9. 对账单
CREATE TABLE reconciliation (
    id BIGINT PRIMARY KEY,
    reconcil_no VARCHAR(64) NOT NULL COMMENT '对账单号',
    supplier_id BIGINT NOT NULL,
    period_start DATE NOT NULL COMMENT '对账周期起',
    period_end DATE NOT NULL COMMENT '对账周期止',
    purchase_total DECIMAL(14,2) DEFAULT 0 COMMENT '采购总额',
    return_total DECIMAL(14,2) DEFAULT 0 COMMENT '退货总额',
    net_amount DECIMAL(14,2) DEFAULT 0 COMMENT '应付净额',
    status TINYINT DEFAULT 0 COMMENT '0待确认 1供应商确认 2采购方确认 3已开票 4已付款',
    confirmed_by VARCHAR(50) COMMENT '确认人',
    confirmed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_reconcil_no (reconcil_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账单';
