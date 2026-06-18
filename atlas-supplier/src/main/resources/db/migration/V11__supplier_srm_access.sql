-- ============================================
-- Atlas V11: 供应商准入管理 (SRM Access)
-- ============================================

-- 1. 招募公告
CREATE TABLE recruit_notice (
    id BIGINT PRIMARY KEY,
    notice_no VARCHAR(64) NOT NULL COMMENT '公告编号',
    title VARCHAR(200) NOT NULL COMMENT '公告标题',
    category_ids TEXT COMMENT '招募品类ID（JSON数组）',
    content TEXT COMMENT '公告内容/技术要求',
    qualification_requirements TEXT COMMENT '资质要求（JSON）',
    publish_time DATETIME COMMENT '发布时间',
    deadline DATETIME COMMENT '截止时间',
    status TINYINT DEFAULT 0 COMMENT '0草稿 1已发布 2已截止 3已关闭',
    publisher_id BIGINT,
    publisher_name VARCHAR(50),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_notice_no (notice_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='招募公告';

-- 2. 供应商注册/准入申请
CREATE TABLE supplier_register (
    id BIGINT PRIMARY KEY,
    recruit_notice_id BIGINT COMMENT '关联招募公告ID',
    supplier_name VARCHAR(100) NOT NULL,
    credit_code VARCHAR(50) COMMENT '统一社会信用代码',
    legal_person VARCHAR(50) COMMENT '法定代表人',
    registered_capital DECIMAL(14,2) COMMENT '注册资本（万元）',
    established_date DATE COMMENT '成立日期',
    business_scope TEXT COMMENT '经营范围',
    company_type VARCHAR(50) COMMENT '企业类型',
    contact_person VARCHAR(50),
    contact_phone VARCHAR(20),
    email VARCHAR(100),
    address VARCHAR(200),
    qualification_files TEXT COMMENT '资质文件URL（JSON）',
    approval_status TINYINT DEFAULT 0 COMMENT '0待审核 1初审通过 2现场考察 3终审通过 4驳回 5已入库',
    reject_reason VARCHAR(500),
    reviewer_id BIGINT,
    reviewer_name VARCHAR(50),
    reviewed_at DATETIME,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_status (approval_status),
    INDEX idx_notice_id (recruit_notice_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商注册/准入申请';

-- 3. 审批记录
CREATE TABLE supplier_approval_record (
    id BIGINT PRIMARY KEY,
    register_id BIGINT NOT NULL COMMENT '注册申请ID',
    approval_node VARCHAR(50) NOT NULL COMMENT '审批节点: INITIAL_REVIEW/FIELD_INSPECT/FINAL_REVIEW',
    approval_result TINYINT NOT NULL COMMENT '1通过 2驳回 3退回修改',
    score DECIMAL(5,2) COMMENT '评分',
    comment TEXT COMMENT '审批意见',
    approver_id BIGINT,
    approver_name VARCHAR(50),
    approver_dept VARCHAR(50) COMMENT '审批部门: QUALITY/TECH/FINANCE',
    approved_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_register_id (register_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商审批记录';
