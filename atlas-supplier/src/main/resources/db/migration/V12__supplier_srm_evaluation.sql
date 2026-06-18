-- ============================================
-- Atlas V12: 供应商绩效评估 (SRM Evaluation)
-- ============================================

-- 4. 评估模板
CREATE TABLE eval_template (
    id BIGINT PRIMARY KEY,
    template_name VARCHAR(100) NOT NULL,
    eval_type TINYINT DEFAULT 1 COMMENT '1季度 2年度 3专项',
    eval_dimensions TEXT NOT NULL COMMENT '评分维度定义（JSON: [{name, category, maxScore, weight}]）',
    min_pass_score DECIMAL(5,2) DEFAULT 60 COMMENT '及格线',
    status TINYINT DEFAULT 1 COMMENT '1启用 0停用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估模板';

-- 5. 绩效考核主表
CREATE TABLE supplier_evaluation (
    id BIGINT PRIMARY KEY,
    supplier_id BIGINT NOT NULL,
    template_id BIGINT,
    eval_period VARCHAR(20) NOT NULL COMMENT '评估周期: 2026-Q1',
    eval_type TINYINT DEFAULT 1 COMMENT '1季度 2年度 3专项',
    quality_score DECIMAL(5,2) DEFAULT 0 COMMENT '质量得分',
    delivery_score DECIMAL(5,2) DEFAULT 0 COMMENT '交付得分',
    cost_score DECIMAL(5,2) DEFAULT 0 COMMENT '成本得分',
    service_score DECIMAL(5,2) DEFAULT 0 COMMENT '服务得分',
    total_score DECIMAL(5,2) DEFAULT 0 COMMENT '综合得分',
    eval_level VARCHAR(4) COMMENT '等级: A/B/C/D',
    improvement_note TEXT COMMENT '整改要求',
    improvement_deadline DATE COMMENT '整改截止日期',
    evaluator_id BIGINT,
    evaluator_name VARCHAR(50),
    status TINYINT DEFAULT 0 COMMENT '0草稿 1已发布 2供应商确认 3整改中 4整改完成 5关闭',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_supplier_period (supplier_id, eval_period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商绩效考核主表';

-- 6. 评估明细项
CREATE TABLE supplier_evaluation_item (
    id BIGINT PRIMARY KEY,
    evaluation_id BIGINT NOT NULL,
    item_name VARCHAR(100) NOT NULL COMMENT '考核项',
    item_category VARCHAR(32) COMMENT '类别: QUALITY/DELIVERY/COST/SERVICE',
    weight DECIMAL(5,2) COMMENT '权重(%)',
    max_score DECIMAL(5,2) DEFAULT 100,
    actual_score DECIMAL(5,2) DEFAULT 0,
    score_source VARCHAR(50) COMMENT '数据来源: ERP/MES/MANUAL',
    data_evidence TEXT COMMENT '评分依据',
    remark VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_eval_id (evaluation_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评估明细项';
