-- ====================================================================
-- Atlas V104: 物料管理 + 供应商管理增强
-- 日期：2026-06-18
-- 说明：物料分类体系、规格属性模板、ERP映射 + 供应商绩效评分卡、分级、风险监控、改善跟踪
-- ====================================================================

-- ==================== 3.1 物料管理 / Material Management ====================

-- 3.1.1 物料分类体系（4级树形结构） / Material Category System (4-level tree)
CREATE TABLE IF NOT EXISTS material_category (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    parent_id BIGINT DEFAULT 0 COMMENT '父分类ID，0表示顶级分类 / Parent category ID, 0 = top-level',
    code VARCHAR(32) NOT NULL COMMENT '分类编码 / Category code',
    name VARCHAR(100) NOT NULL COMMENT '分类名称 / Category name',
    level TINYINT NOT NULL DEFAULT 1 COMMENT '层级: 1大类 2中类 3小类 4细类 / Level: 1-category 2-subcategory 3-class 4-subclass',
    standard_code VARCHAR(32) COMMENT 'UNSPSC标准分类码映射 / UNSPSC standard classification code mapping',
    sort_order INT DEFAULT 0 COMMENT '排序序号 / Sort order',
    status TINYINT DEFAULT 1 COMMENT '状态: 1启用 0停用 / Status: 1=active, 0=inactive',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_category_code (code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_level (level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料分类表（4级树形结构）';

-- 3.1.2 物料规格属性模板 / Material Attribute Template
CREATE TABLE IF NOT EXISTS material_attr_template (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    category_id BIGINT NOT NULL COMMENT '关联分类ID / Associated category ID',
    attr_name VARCHAR(100) NOT NULL COMMENT '属性名称 / Attribute name',
    attr_type VARCHAR(16) NOT NULL DEFAULT 'STRING' COMMENT '属性类型: STRING数值/NUMBER数字/ENUM枚举/DATE日期 / Attribute type',
    is_required TINYINT DEFAULT 0 COMMENT '是否必填: 0否 1是 / Required: 0=no, 1=yes',
    enum_values VARCHAR(500) COMMENT '枚举值列表（逗号分隔，仅ENUM类型有效） / Enum values (comma-separated, ENUM type only)',
    unit VARCHAR(20) COMMENT '单位 / Unit',
    sort_order INT DEFAULT 0 COMMENT '排序序号 / Sort order',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_category_id (category_id),
    UNIQUE KEY uk_category_attr (category_id, attr_name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='物料规格属性模板表';

-- 3.1.5 ERP 物料编码映射 / ERP Material Code Mapping
CREATE TABLE IF NOT EXISTS material_erp_mapping (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    material_id BIGINT NOT NULL COMMENT 'Atlas物料ID / Atlas material ID',
    erp_system VARCHAR(32) NOT NULL COMMENT 'ERP系统: SAP/KINGDEE/U8 / ERP system identifier',
    erp_material_code VARCHAR(64) NOT NULL COMMENT 'ERP物料编码 / ERP material code',
    plant_code VARCHAR(32) COMMENT '工厂编码 / Plant code',
    mapped_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '映射时间 / Mapped time',
    status TINYINT DEFAULT 1 COMMENT '状态: 1有效 0无效 / Status: 1=active, 0=inactive',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_material_id (material_id),
    INDEX idx_erp_code (erp_system, erp_material_code),
    UNIQUE KEY uk_material_erp (material_id, erp_system)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ERP物料编码映射表';

-- goods 表增加 material_category 外键关联 / Add FK from goods to material_category
-- 注意：goods 表已有 category_id 列，此处仅添加外键关系声明 / Note: goods already has category_id; FK declaration only
-- ALTER TABLE goods ADD CONSTRAINT fk_goods_material_category FOREIGN KEY (category_id) REFERENCES material_category(id);
-- 由于 IF NOT EXISTS 对外键不适用且生产环境可能冲突，FK 以逻辑约束为准，应用层校验 / FK is logical constraint; validated at application layer

-- ==================== 3.2 供应商管理 / Supplier Management ====================

-- 3.2.1 供应商绩效评分卡主表 / Supplier Scorecard Master
CREATE TABLE IF NOT EXISTS supplier_scorecard (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    period VARCHAR(20) NOT NULL COMMENT '评分周期: 2026-Q2 / Scoring period',
    quality_score DECIMAL(5,2) DEFAULT 0 COMMENT '质量得分（满分30） / Quality score (max 30)',
    cost_score DECIMAL(5,2) DEFAULT 0 COMMENT '成本得分（满分25） / Cost score (max 25)',
    delivery_score DECIMAL(5,2) DEFAULT 0 COMMENT '交付得分（满分25） / Delivery score (max 25)',
    service_score DECIMAL(5,2) DEFAULT 0 COMMENT '服务得分（满分20） / Service score (max 20)',
    total_score DECIMAL(5,2) DEFAULT 0 COMMENT '综合得分（满分100） / Total score (max 100)',
    grade VARCHAR(4) COMMENT '评级: A/B/C/D / Grade',
    assessor_id BIGINT COMMENT '评分人ID / Assessor ID',
    assessor_name VARCHAR(50) COMMENT '评分人姓名 / Assessor name',
    remark VARCHAR(500) COMMENT '备注 / Remark',
    status TINYINT DEFAULT 0 COMMENT '状态: 0草稿 1已发布 2已确认 / Status: 0=draft, 1=published, 2=confirmed',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_supplier_period (supplier_id, period),
    UNIQUE KEY uk_supplier_period (supplier_id, period)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商绩效评分卡主表';

-- 3.2.1 评分卡明细项 / Scorecard Items (QCD维度明细)
CREATE TABLE IF NOT EXISTS supplier_scorecard_item (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    scorecard_id BIGINT NOT NULL COMMENT '关联评分卡ID / Associated scorecard ID',
    dimension VARCHAR(16) NOT NULL COMMENT '维度: QUALITY/COST/DELIVERY/SERVICE / Dimension',
    item_name VARCHAR(100) NOT NULL COMMENT '评分项名称 / Item name',
    weight DECIMAL(5,2) DEFAULT 0 COMMENT '权重(%) / Weight (%)',
    max_score DECIMAL(5,2) DEFAULT 100 COMMENT '满分 / Maximum score',
    actual_score DECIMAL(5,2) DEFAULT 0 COMMENT '实际得分 / Actual score',
    data_source VARCHAR(100) COMMENT '数据来源 / Data source',
    remark VARCHAR(500) COMMENT '备注 / Remark',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    INDEX idx_scorecard_id (scorecard_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商评分卡明细项表';

-- 3.2.2 供应商分级 / Supplier Classification
CREATE TABLE IF NOT EXISTS supplier_classification (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    grade VARCHAR(16) NOT NULL COMMENT '分级: STRATEGIC战略/CORE核心/GENERAL一般/POTENTIAL潜在 / Grade',
    assessed_date DATE NOT NULL COMMENT '评定日期 / Assessment date',
    valid_until DATE COMMENT '有效期至 / Valid until',
    assessor_id BIGINT COMMENT '评定人ID / Assessor ID',
    assessor_name VARCHAR(50) COMMENT '评定人姓名 / Assessor name',
    reason TEXT COMMENT '评定依据 / Assessment rationale',
    prev_grade VARCHAR(16) COMMENT '前次分级 / Previous grade',
    status TINYINT DEFAULT 1 COMMENT '状态: 1有效 0失效 / Status: 1=active, 0=inactive',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_supplier_grade (supplier_id, grade),
    INDEX idx_valid_until (valid_until)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商分级表';

-- 3.2.3 供应商风险监控预警 / Supplier Risk Alert
CREATE TABLE IF NOT EXISTS supplier_risk_alert (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    risk_type VARCHAR(32) NOT NULL COMMENT '风险类型: BUSINESS_CHANGE工商变更/JUDICIAL司法/OPERATION经营异常/FINANCIAL财务 / Risk type',
    risk_level VARCHAR(16) NOT NULL COMMENT '风险等级: LOW低/MEDIUM中/HIGH高/CRITICAL严重 / Risk level',
    description VARCHAR(500) NOT NULL COMMENT '风险描述 / Risk description',
    source VARCHAR(50) COMMENT '数据来源: QICHACHA企查查/TIANYANCHA天眼查/MANUAL人工 / Data source',
    alert_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预警时间 / Alert time',
    is_resolved TINYINT DEFAULT 0 COMMENT '是否已解决: 0否 1是 / Resolved: 0=no, 1=yes',
    resolved_time DATETIME COMMENT '解决时间 / Resolved time',
    handler_id BIGINT COMMENT '处理人ID / Handler ID',
    handler_name VARCHAR(50) COMMENT '处理人姓名 / Handler name',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_supplier_risk (supplier_id, risk_level),
    INDEX idx_alert_time (alert_time),
    INDEX idx_is_resolved (is_resolved)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商风险预警表';

-- 3.2.4 改善跟踪闭环 / Supplier Improvement (Corrective Action Closed-Loop)
CREATE TABLE IF NOT EXISTS supplier_improvement (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法）',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    issue_type VARCHAR(32) NOT NULL COMMENT '问题类型: QUALITY质量/DELIVERY交付/PERFORMANCE绩效/OTHER其他 / Issue type',
    title VARCHAR(200) NOT NULL COMMENT '整改标题 / Improvement title',
    description TEXT NOT NULL COMMENT '问题描述 / Issue description',
    root_cause TEXT COMMENT '根因分析 / Root cause analysis',
    corrective_action TEXT COMMENT '纠正措施 / Corrective action',
    verifier_id BIGINT COMMENT '验证人ID / Verifier ID',
    verifier_name VARCHAR(50) COMMENT '验证人姓名 / Verifier name',
    deadline DATE COMMENT '整改截止日期 / Improvement deadline',
    status VARCHAR(16) DEFAULT 'OPEN' COMMENT '状态: OPEN开启→IN_PROGRESS进行中→VERIFIED已验证→CLOSED关闭 / Status flow',
    verified_at DATETIME COMMENT '验证时间 / Verified time',
    closed_at DATETIME COMMENT '关闭时间 / Closed time',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_supplier_status (supplier_id, status),
    INDEX idx_deadline (deadline)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商改善跟踪闭环表';
