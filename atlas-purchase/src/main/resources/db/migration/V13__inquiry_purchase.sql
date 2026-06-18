-- ============================================================
-- V13: 询比采购模式
-- 向至少3家供应商发送询价，一次性不可更改报价，比较后选择最优
-- ============================================================

CREATE TABLE IF NOT EXISTS inquiry_purchase (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    inquiry_no VARCHAR(64) NOT NULL COMMENT '询价单号',
    purchase_order_id BIGINT COMMENT '关联采购单ID',
    title VARCHAR(200) NOT NULL COMMENT '询价标题',
    inquiry_content TEXT COMMENT '询价内容/规格要求',
    inquiry_deadline DATE COMMENT '报价截止日期',
    min_supplier_count INT DEFAULT 3 COMMENT '最少询价供应商数',
    status TINYINT DEFAULT 0 COMMENT '0草稿 1询价中 2报价结束 3比较中 4已定标 5终止',
    winner_supplier_id BIGINT COMMENT '成交供应商ID',
    winner_amount DECIMAL(14,2) COMMENT '成交金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_inquiry_no (inquiry_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='询比采购主表';

CREATE TABLE IF NOT EXISTS inquiry_supplier (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    inquiry_id BIGINT NOT NULL COMMENT '关联询价单ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name VARCHAR(100) COMMENT '供应商名称',
    quote_amount DECIMAL(14,2) COMMENT '报价金额',
    delivery_days INT COMMENT '交货天数',
    payment_terms VARCHAR(50) COMMENT '付款条件',
    remark VARCHAR(500) COMMENT '备注',
    quote_time DATETIME COMMENT '报价时间',
    INDEX idx_inquiry_id (inquiry_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='询比采购供应商报价表';
