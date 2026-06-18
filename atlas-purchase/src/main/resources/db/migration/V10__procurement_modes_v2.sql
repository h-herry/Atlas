-- ============================================================
-- V10: 采购方式体系升级 — 从 8 种扩展为 9 种符合政府采购规范的模式
-- 新增竞争性谈判、竞争性磋商、框架协议采购三套完整表结构
-- ============================================================

-- ----------------------------------------------------------
-- 1. 竞争性谈判会话表
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS negotiation_session (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    purchase_order_id BIGINT NOT NULL COMMENT '关联采购订单ID',
    negotiation_no VARCHAR(64) NOT NULL COMMENT '谈判编号',
    title VARCHAR(200) NOT NULL COMMENT '谈判标题',
    content TEXT COMMENT '谈判内容/技术要求',
    invited_supplier_ids TEXT NOT NULL COMMENT '邀请供应商ID（JSON数组，至少2家）',
    negotiation_method VARCHAR(32) DEFAULT 'LOWEST_PRICE' COMMENT '评审方法: LOWEST_PRICE',
    status TINYINT DEFAULT 0 COMMENT '0-草稿 1-已发布 2-谈判中 3-报价收集完成 4-评审中 5-定标 6-终止',
    winner_supplier_id BIGINT COMMENT '成交供应商ID',
    winner_amount DECIMAL(14,2) COMMENT '成交金额',
    round_count INT DEFAULT 0 COMMENT '谈判轮次',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (purchase_order_id),
    UNIQUE KEY uk_no (negotiation_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞争性谈判会话表';

-- ----------------------------------------------------------
-- 2. 谈判报价记录表
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS negotiation_round (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    negotiation_id BIGINT NOT NULL COMMENT '关联谈判会话ID',
    round_no INT NOT NULL COMMENT '轮次',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name VARCHAR(100) COMMENT '供应商名称',
    offer_amount DECIMAL(14,2) NOT NULL COMMENT '报价金额',
    tech_proposal TEXT COMMENT '技术方案',
    delivery_days INT COMMENT '交货天数',
    payment_terms VARCHAR(100) COMMENT '付款条件',
    negotiator_comment TEXT COMMENT '谈判记录/评审意见',
    is_final TINYINT DEFAULT 0 COMMENT '是否最终报价: 0-否 1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_negotiation_id (negotiation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='谈判报价记录表';

-- ----------------------------------------------------------
-- 3. 竞争性磋商会话表
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_session (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    purchase_order_id BIGINT NOT NULL COMMENT '关联采购订单ID',
    consultation_no VARCHAR(64) NOT NULL COMMENT '磋商编号',
    title VARCHAR(200) NOT NULL COMMENT '磋商标题',
    content TEXT COMMENT '磋商内容/需求说明',
    invited_supplier_ids TEXT NOT NULL COMMENT '邀请供应商ID（JSON数组）',
    scoring_rules TEXT COMMENT '综合评分规则（JSON: 各维度权重）',
    status TINYINT DEFAULT 0 COMMENT '0-草稿 1-公告期 2-响应文件提交 3-磋商中 4-最终报价 5-综合评审 6-定标 7-终止',
    winner_supplier_id BIGINT COMMENT '成交供应商ID',
    winner_amount DECIMAL(14,2) COMMENT '成交金额',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_order_id (purchase_order_id),
    UNIQUE KEY uk_no (consultation_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞争性磋商会话表';

-- ----------------------------------------------------------
-- 4. 磋商评审记录表
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS consultation_review (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    consultation_id BIGINT NOT NULL COMMENT '关联磋商会话ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name VARCHAR(100) COMMENT '供应商名称',
    tech_score DECIMAL(5,2) DEFAULT 0 COMMENT '技术得分',
    business_score DECIMAL(5,2) DEFAULT 0 COMMENT '商务得分',
    price_score DECIMAL(5,2) DEFAULT 0 COMMENT '价格得分',
    total_score DECIMAL(5,2) DEFAULT 0 COMMENT '综合得分',
    final_offer DECIMAL(14,2) COMMENT '最终报价',
    rank INT COMMENT '排名',
    reviewer_id BIGINT COMMENT '评审人ID',
    reviewer_name VARCHAR(50) COMMENT '评审人姓名',
    review_comment TEXT COMMENT '评审意见',
    is_winner TINYINT DEFAULT 0 COMMENT '是否中标: 0-否 1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='磋商评审记录表';

-- ----------------------------------------------------------
-- 5. 框架协议主表
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS framework_agreement (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    agreement_no VARCHAR(64) NOT NULL COMMENT '协议编号',
    agreement_name VARCHAR(200) NOT NULL COMMENT '协议名称',
    procurement_type TINYINT DEFAULT 1 COMMENT '采购类别',
    estimated_total_amount DECIMAL(14,2) COMMENT '预估总金额',
    valid_from DATE NOT NULL COMMENT '有效期起',
    valid_to DATE NOT NULL COMMENT '有效期止',
    supplier_selection_method TINYINT COMMENT '入围方式: 1-公开招标 2-邀请招标 3-竞争性磋商',
    status TINYINT DEFAULT 0 COMMENT '0-草稿 1-入围征集中 2-评审中 3-入围确定 4-协议生效 5-执行中 6-到期 7-终止',
    max_supplier_count INT DEFAULT 0 COMMENT '最大入围供应商数',
    actual_supplier_count INT DEFAULT 0 COMMENT '实际入围供应商数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_no (agreement_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='框架协议主表';

-- ----------------------------------------------------------
-- 6. 框架协议供应商表
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS framework_supplier (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    agreement_id BIGINT NOT NULL COMMENT '关联框架协议ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    supplier_name VARCHAR(100) COMMENT '供应商名称',
    agreed_discount DECIMAL(5,2) COMMENT '约定折扣率(%)',
    max_single_amount DECIMAL(14,2) COMMENT '单次上限金额',
    rank INT DEFAULT 0 COMMENT '入围排名',
    status TINYINT DEFAULT 1 COMMENT '1-入围 2-暂停 3-退出',
    joined_at DATETIME COMMENT '入围日期',
    INDEX idx_agreement_id (agreement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='框架协议供应商表';

-- ----------------------------------------------------------
-- 7. 框架协议二次订单表
-- ----------------------------------------------------------
CREATE TABLE IF NOT EXISTS framework_order (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    agreement_id BIGINT NOT NULL COMMENT '关联框架协议ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID',
    order_no VARCHAR(64) NOT NULL COMMENT '二次订单号',
    order_amount DECIMAL(14,2) NOT NULL COMMENT '订单金额',
    order_content TEXT COMMENT '采购内容',
    select_method TINYINT COMMENT '成交方式: 1-直接选定 2-二次竞价 3-比例分配',
    status TINYINT DEFAULT 0 COMMENT '0-草稿 1-已下单 2-已确认 3-履约中 4-完成',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_agreement_id (agreement_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='框架协议二次订单表';

-- ----------------------------------------------------------
-- 8. 采购订单新增 procurement_type 字段（若不存在则新增）
-- ----------------------------------------------------------
ALTER TABLE purchase_order ADD COLUMN procurement_type TINYINT DEFAULT 1 COMMENT '采购方式: 1-公开招标 2-邀请招标 3-询比采购 4-竞价采购 5-竞争性谈判 6-竞争性磋商 7-单一来源采购 8-框架协议采购 9-合作创新采购';
