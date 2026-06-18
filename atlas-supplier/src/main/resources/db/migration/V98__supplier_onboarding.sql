-- ============================================
-- Atlas V98: 供应商准入双通道 — 自助注册 + 采购员代注册 /
-- Supplier Onboarding Dual Channel — Self-registration + Purchaser Proxy Registration
-- ============================================

-- 1. 供应商入驻申请表（门户端） — sup_portal_register /
-- Supplier onboarding application table (portal) — sup_portal_register
CREATE TABLE IF NOT EXISTS sup_portal_register (
    id                  BIGINT PRIMARY KEY COMMENT '申请ID / Application ID',
    company_name        VARCHAR(200) NOT NULL COMMENT '公司名称 / Company name',
    credit_code         VARCHAR(50) COMMENT '统一社会信用代码 / Unified social credit code',
    legal_person        VARCHAR(100) COMMENT '法定代表人 / Legal representative',
    contact_name        VARCHAR(100) COMMENT '联系人姓名 / Contact name',
    contact_phone       VARCHAR(20) COMMENT '联系人电话 / Contact phone',
    contact_email       VARCHAR(200) COMMENT '联系人邮箱 / Contact email',
    industry_category   VARCHAR(100) COMMENT '行业类别 / Industry category',
    main_products       VARCHAR(500) COMMENT '主营产品 / Main products',
    annual_revenue      DECIMAL(14,2) COMMENT '年营收（元） / Annual revenue (CNY)',
    employee_count      INT COMMENT '员工人数 / Employee count',
    certificates        TEXT COMMENT '资质证书列表（JSON数组） / Certificate list (JSON array)',
    source              VARCHAR(20) DEFAULT 'SELF' COMMENT '来源：SELF-自助注册 / PURCHASER-采购员代注册 / Channel: SELF=Self-registration, PURCHASER=Proxy by purchaser',
    initiator_id        BIGINT COMMENT '发起人ID（采购员代注册时） / Initiator ID (for purchaser proxy registration)',
    initiator_name      VARCHAR(100) COMMENT '发起人姓名 / Initiator name',
    process_instance_id VARCHAR(64) COMMENT 'Flowable流程实例ID / Flowable process instance ID',
    apply_status        TINYINT DEFAULT 0 COMMENT '申请状态: 0待审批 1审批中 2已通过 3已驳回 4已撤回 / Status: 0=pending, 1=in review, 2=approved, 3=rejected, 4=withdrawn',
    supplier_id         BIGINT COMMENT '审批通过后关联的供应商主数据ID / Supplier master data ID after approval',
    reject_reason       VARCHAR(500) COMMENT '驳回原因 / Reject reason',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at          DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_source (source),
    INDEX idx_apply_status (apply_status),
    INDEX idx_process_instance_id (process_instance_id),
    INDEX idx_initiator_id (initiator_id),
    INDEX idx_credit_code (credit_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商入驻申请表（门户端双通道） / Supplier onboarding application table (portal dual-channel)';

-- 2. 入驻审批节点记录表 / Onboarding approval node record table
CREATE TABLE IF NOT EXISTS sup_onboarding_approval (
    id                  BIGINT PRIMARY KEY COMMENT '审批记录ID / Approval record ID',
    register_id         BIGINT NOT NULL COMMENT '入驻申请ID / Registration application ID',
    task_id             VARCHAR(64) COMMENT 'Flowable任务ID / Flowable task ID',
    approval_node       VARCHAR(50) NOT NULL COMMENT '审批节点: INITIAL_REVIEW / FIELD_INSPECT / FINAL_REVIEW / Approval node',
    approval_result     TINYINT COMMENT '审批结果: 1通过 2驳回 / Approval result: 1=approved, 2=rejected',
    comment             TEXT COMMENT '审批意见 / Approval comment',
    approver_id         BIGINT COMMENT '审批人ID / Approver ID',
    approver_name       VARCHAR(100) COMMENT '审批人姓名 / Approver name',
    approver_role       VARCHAR(50) COMMENT '审批人角色：PURCHASE_SUPERVISOR / QC_INSPECTOR / PURCHASE_DIRECTOR / Approver role',
    approved_at         DATETIME COMMENT '审批时间 / Approval time',
    created_at          DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间 / Record created time',
    INDEX idx_register_id (register_id),
    INDEX idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='入驻审批节点记录表 / Onboarding approval node record table';
