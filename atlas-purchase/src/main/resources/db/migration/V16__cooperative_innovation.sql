-- ============================================================
-- V16: 合作创新采购模式
-- 邀请供应商合作研发创新产品，共担风险并承诺购买成果，分阶段管理
-- ============================================================

CREATE TABLE IF NOT EXISTS cooperative_innovation (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    innovation_no VARCHAR(64) NOT NULL COMMENT '创新采购编号',
    purchase_order_id BIGINT COMMENT '关联采购单ID',
    title VARCHAR(200) NOT NULL COMMENT '创新采购标题',
    rd_content TEXT COMMENT '研发内容',
    rd_budget DECIMAL(14,2) COMMENT '研发预算',
    rd_cycle VARCHAR(50) COMMENT '研发周期',
    ip_ownership VARCHAR(32) DEFAULT 'SHARED' COMMENT '知识产权归属: BUYER/SUPPLIER/SHARED',
    stage_count INT DEFAULT 3 COMMENT '研发阶段数',
    status TINYINT DEFAULT 0 COMMENT '0征集中 1评审中 2合作中 3验收中 4已完成 5终止',
    partner_supplier_id BIGINT COMMENT '合作供应商ID',
    stage_progress VARCHAR(500) COMMENT '各阶段进度',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_innovation_no (innovation_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合作创新采购表';
