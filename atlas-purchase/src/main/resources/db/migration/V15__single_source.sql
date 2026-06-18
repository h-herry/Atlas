-- ============================================================
-- V15: 单一来源采购模式
-- 不经过竞争直接与唯一供应商谈判，适用于专利技术/紧急情况/原项目配套
-- ============================================================

CREATE TABLE IF NOT EXISTS single_source_purchase (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    purchase_order_id BIGINT COMMENT '关联采购单ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name VARCHAR(100) COMMENT '供应商名称',
    single_source_reason TEXT NOT NULL COMMENT '单一来源理由',
    negotiation_amount DECIMAL(14,2) COMMENT '谈判金额',
    final_amount DECIMAL(14,2) COMMENT '最终成交金额',
    status TINYINT DEFAULT 0 COMMENT '0草稿 1谈判中 2已成交 3终止',
    negotiated_by VARCHAR(50) COMMENT '谈判人',
    negotiated_at DATETIME COMMENT '谈判时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='单一来源采购表';
