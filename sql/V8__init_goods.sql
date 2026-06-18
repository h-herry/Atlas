-- ====================================================================
-- Atlas 商品主数据初始化脚本
-- 版本：V8
-- 说明：创建商品分类表和商品主表（SKU），补建采购系统商品主数据模型
-- 日期：2026-06-17
-- ====================================================================

-- 商品分类表
CREATE TABLE IF NOT EXISTS goods_category (
    id BIGINT PRIMARY KEY,
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类',
    category_name VARCHAR(100) NOT NULL COMMENT '分类名称',
    category_code VARCHAR(32) NOT NULL COMMENT '分类编码',
    sort_order INT DEFAULT 0 COMMENT '排序序号',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_category_code (category_code),
    INDEX idx_parent_id (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

-- 商品主表（SKU）
CREATE TABLE IF NOT EXISTS goods (
    id BIGINT PRIMARY KEY,
    goods_name VARCHAR(200) NOT NULL COMMENT '商品名称',
    goods_code VARCHAR(64) NOT NULL COMMENT '商品编码（SKU编码）',
    category_id BIGINT COMMENT '所属分类ID',
    spec VARCHAR(200) COMMENT '规格型号',
    unit VARCHAR(20) COMMENT '单位（个/件/箱/kg等）',
    brand VARCHAR(100) COMMENT '品牌',
    default_price DECIMAL(14,2) DEFAULT 0.00 COMMENT '默认价格',
    status TINYINT DEFAULT 1 COMMENT '状态：1启用 0停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_goods_code (goods_code),
    INDEX idx_category (category_id),
    INDEX idx_name (goods_name(50))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品主表（SKU）';
