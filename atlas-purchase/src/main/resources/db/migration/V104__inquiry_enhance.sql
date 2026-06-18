-- ====================================================================
-- Atlas V104: 询报价管理增强
-- 日期：2026-06-18
-- 说明：询价模板管理 + 多维度比价 + 历史价格趋势
-- ====================================================================

-- ==================== 3.3 询报价管理 / Inquiry Management ====================

-- 3.3.1 询价模板 / Inquiry Template
CREATE TABLE IF NOT EXISTS inquiry_template (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    template_id VARCHAR(32) NOT NULL COMMENT '模板编号 / Template number',
    name VARCHAR(200) NOT NULL COMMENT '模板名称 / Template name',
    category_id BIGINT COMMENT '关联物料分类ID / Associated material category ID',
    delivery_required TINYINT DEFAULT 1 COMMENT '交期是否必填: 0否 1是 / Delivery required: 0=no, 1=yes',
    quality_required TINYINT DEFAULT 1 COMMENT '质量资质是否必填: 0否 1是 / Quality cert required: 0=no, 1=yes',
    price_breakdown_enabled TINYINT DEFAULT 0 COMMENT '是否启用价格明细: 0否 1是 / Price breakdown enabled: 0=no, 1=yes',
    attr_field_ids TEXT COMMENT '关联的属性模板字段ID列表（JSON数组） / Linked attribute template field IDs (JSON array)',
    status TINYINT DEFAULT 1 COMMENT '状态: 1启用 0停用 / Status: 1=active, 0=inactive',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_template_id (template_id),
    INDEX idx_category_id (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='询价模板表';
