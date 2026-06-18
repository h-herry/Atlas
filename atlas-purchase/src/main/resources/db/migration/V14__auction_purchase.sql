-- ============================================================
-- V14: 竞价采购模式
-- 反向拍卖模式：供应商多轮降价竞价，支持自动延时，以最终最低价定标
-- ============================================================

CREATE TABLE IF NOT EXISTS auction_purchase (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    auction_no VARCHAR(64) NOT NULL COMMENT '竞价编号',
    purchase_order_id BIGINT COMMENT '关联采购单ID',
    title VARCHAR(200) NOT NULL COMMENT '竞价标题',
    auction_content TEXT COMMENT '竞价内容/技术要求',
    start_price DECIMAL(14,2) COMMENT '起拍价/最高限价',
    min_decrement DECIMAL(14,2) COMMENT '最小降价幅度',
    start_time DATETIME COMMENT '竞价开始时间',
    end_time DATETIME COMMENT '竞价结束时间',
    auction_type VARCHAR(32) DEFAULT 'REVERSE' COMMENT 'REVERSE反向竞价/FORWARD正向竞价',
    auto_extend TINYINT DEFAULT 0 COMMENT '最后时刻报价是否自动延时',
    extend_minutes INT DEFAULT 5 COMMENT '自动延时分钟数',
    status TINYINT DEFAULT 0 COMMENT '0准备中 1竞价中 2已结束 3已定标 4终止',
    winner_supplier_id BIGINT COMMENT '成交供应商ID',
    winner_amount DECIMAL(14,2) COMMENT '成交金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_auction_no (auction_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞价采购主表';

CREATE TABLE IF NOT EXISTS auction_bid (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    auction_id BIGINT NOT NULL COMMENT '关联竞价ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name VARCHAR(100) COMMENT '供应商名称',
    bid_amount DECIMAL(14,2) NOT NULL COMMENT '出价金额',
    bid_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '出价时间',
    is_valid TINYINT DEFAULT 1 COMMENT '是否有效出价',
    INDEX idx_auction (auction_id, bid_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞价出价记录表';
