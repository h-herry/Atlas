-- ====================================================================
-- Atlas 物料管理模块 — V19: 供应商发货协同 + 生产进度
-- 日期：2026-06-17
-- ====================================================================

-- 供应商发货单主表
CREATE TABLE IF NOT EXISTS supplier_delivery (
    id BIGINT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL COMMENT '关联采购订单ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    delivery_no VARCHAR(64) NOT NULL COMMENT '发货单号',
    status TINYINT DEFAULT 0 COMMENT '0待发货 1已发货 2部分收货 3已收货',
    tracking_no VARCHAR(100) COMMENT '物流单号',
    carrier VARCHAR(50) COMMENT '承运商',
    estimated_arrival DATE COMMENT '预计到货日期',
    shipped_at DATETIME COMMENT '发货时间',
    barcode_label_url VARCHAR(500) COMMENT '条码标签URL',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_delivery_no (delivery_no),
    INDEX idx_order (purchase_order_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商发货单';

-- 供应商发货明细表
CREATE TABLE IF NOT EXISTS supplier_delivery_item (
    id BIGINT PRIMARY KEY,
    delivery_id BIGINT NOT NULL COMMENT '发货单ID',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    delivery_qty DECIMAL(14,2) NOT NULL COMMENT '发货数量',
    batch_no VARCHAR(64) COMMENT '生产批次号',
    production_date DATE COMMENT '生产日期',
    expiry_date DATE COMMENT '有效期至',
    barcode VARCHAR(128) COMMENT '物料条码',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_delivery (delivery_id),
    INDEX idx_material (material_id),
    INDEX idx_batch (batch_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商发货明细表';

-- 供应商生产进度填报表
CREATE TABLE IF NOT EXISTS production_progress (
    id BIGINT PRIMARY KEY,
    purchase_order_id BIGINT NOT NULL COMMENT '采购订单ID',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    total_qty DECIMAL(14,2) COMMENT '订单总量',
    produced_qty DECIMAL(14,2) COMMENT '已生产量',
    progress_percent DECIMAL(5,2) COMMENT '进度百分比',
    estimated_completion DATE COMMENT '预计完成日期',
    report_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '填报时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order (purchase_order_id),
    INDEX idx_supplier_material (supplier_id, material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商生产进度填报表';
