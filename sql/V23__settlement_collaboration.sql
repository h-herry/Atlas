-- ============================================
-- Atlas V23: 财务结算协同 + 预测计划协同
-- 数据库: atlas_supplier
-- ============================================

USE atlas_supplier;

-- 财务结算单表
CREATE TABLE IF NOT EXISTS settlement_bill (
    id                      BIGINT PRIMARY KEY COMMENT '结算单ID',
    bill_no                 VARCHAR(64) NOT NULL COMMENT '结算单号',
    supplier_id             BIGINT NOT NULL COMMENT '供应商ID',
    period_start            DATE COMMENT '结算周期开始',
    period_end              DATE COMMENT '结算周期结束',
    bill_amount             DECIMAL(14,2) COMMENT '结算金额',
    status                  TINYINT DEFAULT 0 COMMENT '0待对账 1供应商已确认 2采购方已确认 3已结算',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bill_no (bill_no),
    INDEX idx_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='财务结算单表';

-- 三单匹配表 (订单/入库单/发票)
CREATE TABLE IF NOT EXISTS settlement_three_way_match (
    id                      BIGINT PRIMARY KEY COMMENT '匹配记录ID',
    settlement_id           BIGINT NOT NULL COMMENT '结算单ID',
    purchase_order_id       BIGINT NOT NULL COMMENT '采购订单ID',
    receipt_id              BIGINT COMMENT '入库单ID',
    invoice_no              VARCHAR(64) COMMENT '发票号',
    order_amount            DECIMAL(14,2) COMMENT '订单金额',
    receipt_amount          DECIMAL(14,2) COMMENT '入库金额',
    invoice_amount          DECIMAL(14,2) COMMENT '发票金额',
    match_result            VARCHAR(32) COMMENT '匹配结果: MATCH一致/MISMATCH差异',
    difference_desc         VARCHAR(500) COMMENT '差异说明',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_settlement (settlement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三单匹配表';

-- 需求预测表
CREATE TABLE IF NOT EXISTS demand_forecast (
    id                      BIGINT PRIMARY KEY COMMENT '预测ID',
    forecast_no             VARCHAR(64) NOT NULL COMMENT '预测编号',
    material_id             BIGINT NOT NULL COMMENT '物料ID',
    forecast_qty            DECIMAL(14,2) COMMENT '预测数量',
    forecast_month          VARCHAR(7) COMMENT '预测月份(YYYY-MM)',
    confidence_level        VARCHAR(16) COMMENT '置信度: HIGH/MEDIUM/LOW',
    source                  VARCHAR(32) COMMENT '来源: SALES预测/PLAN计划/HISTORY历史',
    shared_to_supplier      TINYINT DEFAULT 0 COMMENT '是否已分享给供应商',
    supplier_feedback_qty   DECIMAL(14,2) COMMENT '供应商承诺量',
    supplier_feedback_date  DATE COMMENT '供应商反馈日期',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_forecast_no (forecast_no),
    INDEX idx_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='需求预测表';
