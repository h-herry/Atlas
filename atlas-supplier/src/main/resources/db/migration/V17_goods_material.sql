-- ====================================================================
-- Atlas 物料管理模块 — V17: 物料主数据扩展
-- 日期：2026-06-17
-- 说明：扩展已有 goods 表为物料主数据，新增 material_unit 多单位换算表
-- ====================================================================

-- 扩展 goods 表，增加物料管理字段
ALTER TABLE goods
    ADD COLUMN IF NOT EXISTS material_type VARCHAR(32) DEFAULT 'BATCH' COMMENT '物料类型: UNIQUE唯一料/BATCH批次料',
    ADD COLUMN IF NOT EXISTS safety_stock DECIMAL(14,2) DEFAULT 0.00 COMMENT '安全库存',
    ADD COLUMN IF NOT EXISTS min_order_qty DECIMAL(14,2) DEFAULT 1.00 COMMENT '最小订货量',
    ADD COLUMN IF NOT EXISTS lead_time_days INT DEFAULT 0 COMMENT '采购提前期(天)';

-- 添加物料编码唯一索引（如不存在）
-- goods_code 可复用为物料编码，已有 uk_goods_code 唯一索引

-- 多单位换算表
CREATE TABLE IF NOT EXISTS material_unit (
    id BIGINT PRIMARY KEY,
    material_id BIGINT NOT NULL COMMENT '物料ID（关联 goods.id）',
    unit_name VARCHAR(20) NOT NULL COMMENT '单位名称（个/箱/托/kg等）',
    conversion_rate DECIMAL(14,4) NOT NULL COMMENT '换算率（相对于基本单位）',
    usage_scene VARCHAR(32) COMMENT '使用场景: PURCHASE采购/STOCK库存/ISSUE发料',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_material (material_id),
    UNIQUE KEY uk_material_unit (material_id, unit_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料多单位换算表';
