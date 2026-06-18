-- ============================================================
-- V11: 公开招标采购模式
-- 完整招标流程：发布→投标→开标→评标→定标，支持最低价/综合评分/性价比三种评标办法
-- ============================================================

CREATE TABLE IF NOT EXISTS open_bidding (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    bid_no VARCHAR(64) NOT NULL COMMENT '招标编号',
    purchase_order_id BIGINT COMMENT '关联采购单ID',
    title VARCHAR(200) NOT NULL COMMENT '招标项目名称',
    bid_content TEXT COMMENT '招标内容/技术要求',
    bid_start_date DATE COMMENT '招标开始日期',
    bid_end_date DATE COMMENT '投标截止日期',
    bid_opening_date DATE COMMENT '开标日期',
    budget_amount DECIMAL(14,2) COMMENT '预算金额',
    bid_deposit DECIMAL(14,2) DEFAULT 0 COMMENT '投标保证金',
    evaluation_method VARCHAR(32) DEFAULT 'MIN_PRICE' COMMENT '评标办法: MIN_PRICE/SCORE/BEST_VALUE',
    status TINYINT DEFAULT 0 COMMENT '0准备中 1公告期 2投标中 3开标中 4评标中 5定标 6流标 7终止',
    winner_supplier_id BIGINT COMMENT '中标供应商ID',
    winner_amount DECIMAL(14,2) COMMENT '中标金额',
    publisher_id BIGINT COMMENT '发布人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_bid_no (bid_no),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公开招标主表';

CREATE TABLE IF NOT EXISTS open_bidding_supplier (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    bidding_id BIGINT NOT NULL COMMENT '关联招标ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name VARCHAR(100) COMMENT '供应商名称',
    bid_amount DECIMAL(14,2) COMMENT '投标报价',
    bid_file_url VARCHAR(500) COMMENT '投标文件URL',
    submit_time DATETIME COMMENT '提交时间',
    is_qualified TINYINT DEFAULT 0 COMMENT '0待审核 1合格 2不合格',
    eval_score DECIMAL(5,2) COMMENT '评标得分',
    eval_comment TEXT COMMENT '评标意见',
    INDEX idx_bidding_id (bidding_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公开招标供应商投标记录表';
