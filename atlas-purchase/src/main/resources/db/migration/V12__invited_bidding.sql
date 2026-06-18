-- ============================================================
-- V12: 邀请招标采购模式
-- 向特定供应商发出邀请，被邀请方接受后投标，后续流程与公开招标类似
-- ============================================================

CREATE TABLE IF NOT EXISTS invited_bidding (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    bid_no VARCHAR(64) NOT NULL COMMENT '招标编号',
    purchase_order_id BIGINT COMMENT '关联采购单ID',
    title VARCHAR(200) NOT NULL COMMENT '招标项目名称',
    bid_content TEXT COMMENT '招标内容/技术要求',
    bid_end_date DATE COMMENT '投标截止日期',
    bid_opening_date DATE COMMENT '开标日期',
    budget_amount DECIMAL(14,2) COMMENT '预算金额',
    invitation_reason TEXT COMMENT '采用邀请招标理由',
    min_invite_count INT DEFAULT 3 COMMENT '最少邀请数量',
    status TINYINT DEFAULT 0 COMMENT '0准备中 1邀请中 2投标中 3开标 4评标 5定标 6终止',
    winner_supplier_id BIGINT COMMENT '中标供应商ID',
    winner_amount DECIMAL(14,2) COMMENT '中标金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_bid_no (bid_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请招标主表';

CREATE TABLE IF NOT EXISTS invited_bidding_supplier (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    bidding_id BIGINT NOT NULL COMMENT '关联招标ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name VARCHAR(100) COMMENT '供应商名称',
    invite_status TINYINT DEFAULT 0 COMMENT '0已邀请 1接受 2拒绝',
    bid_amount DECIMAL(14,2) COMMENT '投标报价',
    bid_file_url VARCHAR(500) COMMENT '投标文件URL',
    submit_time DATETIME COMMENT '提交时间',
    eval_score DECIMAL(5,2) COMMENT '评标得分',
    INDEX idx_bidding_id (bidding_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='邀请招标供应商记录表';
