-- ============================================
-- Atlas V13: 供应商智能推荐
-- 数据库: atlas_purchase
-- ============================================

-- 供应商推荐表
CREATE TABLE supplier_recommendation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    material_id BIGINT NOT NULL COMMENT '物料ID',
    recommended_supplier_id BIGINT NOT NULL COMMENT '推荐供应商ID',
    recommendation_type VARCHAR(32) COMMENT '推荐类型: HISTORY/CATEGORY/REGION/CERT',
    match_score DECIMAL(5,2) COMMENT '匹配度 0-100',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_material (material_id)
) COMMENT='供应商智能推荐';
