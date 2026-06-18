-- ====================================================================
-- Atlas 物料管理模块 — V20: 来料检验IQC + 全链路追溯
-- 日期：2026-06-17
-- ====================================================================

-- 来料检验 IQC 表
CREATE TABLE IF NOT EXISTS iqc_inspection (
    id BIGINT PRIMARY KEY,
    inspection_no VARCHAR(64) NOT NULL COMMENT '检验单号',
    delivery_id BIGINT COMMENT '关联发货单ID',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    batch_no VARCHAR(64) COMMENT '生产批次号',
    inspection_qty DECIMAL(14,2) COMMENT '报检数量',
    sample_qty DECIMAL(14,2) COMMENT '抽样数量',
    qualified_qty DECIMAL(14,2) COMMENT '合格数量',
    defective_qty DECIMAL(14,2) COMMENT '不合格数量',
    inspection_standard TEXT COMMENT '检验标准',
    result VARCHAR(32) COMMENT 'PASS合格/REJECT退货/REWORK返工/ACCEPT让步接收',
    inspector_id BIGINT COMMENT '检验员ID',
    inspected_at DATETIME COMMENT '检验时间',
    remark VARCHAR(500) COMMENT '备注',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_inspection_no (inspection_no),
    INDEX idx_delivery (delivery_id),
    INDEX idx_material (material_id),
    INDEX idx_result (result)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='来料检验IQC表';

-- 物料全链路追溯表
CREATE TABLE IF NOT EXISTS material_trace (
    id BIGINT PRIMARY KEY,
    trace_no VARCHAR(64) NOT NULL COMMENT '追溯号',
    material_id BIGINT NOT NULL COMMENT '物料ID',
    batch_no VARCHAR(64) COMMENT '生产批次号',
    barcode VARCHAR(128) COMMENT '物料条码',
    trace_type VARCHAR(32) COMMENT 'RECEIVE入库/ISSUE出库/PRODUCE生产/INSPECT质检/RETURN退货',
    source_id BIGINT COMMENT '来源单据ID',
    source_no VARCHAR(64) COMMENT '来源单据号',
    quantity DECIMAL(14,2) COMMENT '变动数量',
    warehouse_id BIGINT COMMENT '库位ID',
    operator_id BIGINT COMMENT '操作人ID',
    operated_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_material_batch (material_id, batch_no),
    INDEX idx_trace_no (trace_no),
    INDEX idx_source (source_id, source_no),
    INDEX idx_trace_type (trace_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料全链路追溯表';
