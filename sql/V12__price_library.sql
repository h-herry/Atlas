-- ============================================
-- Atlas V12: 价格库
-- 数据库: atlas_purchase
-- ============================================

USE atlas_purchase;

-- 价格库表
CREATE TABLE IF NOT EXISTS price_library (
    id                      BIGINT PRIMARY KEY COMMENT '价格记录ID',
    material_id             BIGINT NOT NULL COMMENT '物料ID',
    supplier_id             BIGINT NOT NULL COMMENT '供应商ID',
    unit_price              DECIMAL(14,4) NOT NULL COMMENT '单价',
    currency                VARCHAR(8) DEFAULT 'CNY' COMMENT '币种',
    price_type              VARCHAR(32) COMMENT '价格类型: CONTRACT合同价/QUOTATION报价/SPOT现货/AGREEMENT协议价',
    valid_from              DATE COMMENT '有效期起始',
    valid_to                DATE COMMENT '有效期截止',
    source_order_id         BIGINT COMMENT '来源订单ID',
    status                  TINYINT DEFAULT 1 COMMENT '1有效 0无效',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_material (material_id),
    INDEX idx_supplier_material (supplier_id, material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格库表';

-- 价格走势表
CREATE TABLE IF NOT EXISTS price_trend (
    id                      BIGINT PRIMARY KEY COMMENT '走势记录ID',
    material_id             BIGINT NOT NULL COMMENT '物料ID',
    period                  VARCHAR(16) COMMENT '统计周期(YYYY-MM)',
    avg_price               DECIMAL(14,4) COMMENT '平均价格',
    min_price               DECIMAL(14,4) COMMENT '最低价格',
    max_price               DECIMAL(14,4) COMMENT '最高价格',
    transaction_count       INT DEFAULT 0 COMMENT '交易次数',
    trend_direction         VARCHAR(16) COMMENT '走势方向: RISE/FALL/STABLE',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_material_period (material_id, period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='价格走势表';

-- 供应商智能推荐表
CREATE TABLE IF NOT EXISTS supplier_recommendation (
    id                      BIGINT PRIMARY KEY COMMENT '推荐记录ID',
    material_id             BIGINT NOT NULL COMMENT '物料ID',
    recommended_supplier_id BIGINT NOT NULL COMMENT '推荐供应商ID',
    recommendation_type     VARCHAR(32) COMMENT '推荐类型: HISTORY历史交易/CATEGORY同类品类/REGION同区域/CERT资质匹配',
    match_score             DECIMAL(5,2) COMMENT '匹配度(0-100)',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商智能推荐表';
