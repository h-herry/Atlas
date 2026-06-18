-- ====================================================================
-- Atlas 物料管理模块 — V18: BOM协同 + MRP需求计划
-- 日期：2026-06-17
-- ====================================================================

-- BOM 主表
CREATE TABLE IF NOT EXISTS bom (
    id BIGINT PRIMARY KEY,
    bom_code VARCHAR(64) NOT NULL COMMENT 'BOM编号',
    product_name VARCHAR(200) NOT NULL COMMENT '产品名称',
    version VARCHAR(32) DEFAULT 'V1' COMMENT '版本号',
    status TINYINT DEFAULT 0 COMMENT '0编辑中 1已发布 2已归档',
    created_by BIGINT COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_bom_code (bom_code, version),
    INDEX idx_product_name (product_name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BOM主表';

-- BOM 明细表
CREATE TABLE IF NOT EXISTS bom_item (
    id BIGINT PRIMARY KEY,
    bom_id BIGINT NOT NULL COMMENT 'BOM ID',
    material_id BIGINT NOT NULL COMMENT '物料ID（关联 goods.id）',
    quantity DECIMAL(14,4) NOT NULL COMMENT '用量',
    unit VARCHAR(20) COMMENT '单位',
    scrap_rate DECIMAL(5,2) DEFAULT 0.00 COMMENT '损耗率(%)',
    is_key_item TINYINT DEFAULT 0 COMMENT '是否关键物料: 0否 1是',
    substitute_material_id BIGINT COMMENT '替代物料ID',
    remark VARCHAR(200) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_bom (bom_id),
    INDEX idx_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='BOM明细表';

-- MRP 需求计划主表
CREATE TABLE IF NOT EXISTS mrp_plan (
    id BIGINT PRIMARY KEY,
    plan_no VARCHAR(64) NOT NULL COMMENT '计划编号',
    plan_type VARCHAR(32) NOT NULL COMMENT 'MPS主计划/MRP物料计划',
    period_start DATE NOT NULL COMMENT '计划周期开始',
    period_end DATE NOT NULL COMMENT '计划周期结束',
    status TINYINT DEFAULT 0 COMMENT '0草稿 1已计算 2已确认 3已下发',
    created_by BIGINT COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_plan_no (plan_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MRP需求计划主表';

-- MRP 计算结果表
CREATE TABLE IF NOT EXISTS mrp_result (
    id BIGINT PRIMARY KEY,
    plan_id BIGINT NOT NULL COMMENT 'MRP计划ID',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    gross_demand DECIMAL(14,2) COMMENT '毛需求',
    current_stock DECIMAL(14,2) COMMENT '当前库存',
    in_transit DECIMAL(14,2) COMMENT '在途量',
    net_demand DECIMAL(14,2) COMMENT '净需求',
    planned_order_qty DECIMAL(14,2) COMMENT '建议采购量',
    planned_start_date DATE COMMENT '建议开始日期',
    planned_end_date DATE COMMENT '建议到货日期',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_plan_material (plan_id, material_id),
    INDEX idx_plan (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='MRP计算结果表';
