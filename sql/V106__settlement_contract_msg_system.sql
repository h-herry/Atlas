-- ====================================================================
-- Atlas V106: 结算管理 + 合同管理 + 消息中心 + 系统公共模块 P0+P1 优化
-- 基于 09 报告 3.7 / 3.8 / 3.9 / 3.10 章节
-- 日期：2026-06-18
-- Atlas V106: Settlement + Contract + Message + System Common P0+P1 Optimization
-- Based on 09 Report sections 3.7 / 3.8 / 3.9 / 3.10
-- ====================================================================

-- ============================================================
-- 3.7 结算管理 / Settlement Management
-- ============================================================

-- P0-3.7.1 三单匹配主表 / Three-way match main table
CREATE TABLE IF NOT EXISTS three_way_match (
    match_id BIGINT PRIMARY KEY COMMENT '匹配记录ID / Match record ID',
    po_id BIGINT NOT NULL COMMENT '采购订单ID / Purchase order ID',
    po_line_id BIGINT COMMENT '订单行项ID / PO line item ID',
    receive_id BIGINT NOT NULL COMMENT '收货记录ID / Receipt record ID',
    receive_line_id BIGINT COMMENT '收货行项ID / Receipt line item ID',
    invoice_id BIGINT NOT NULL COMMENT '发票ID / Invoice ID',
    invoice_line_id BIGINT COMMENT '发票行项ID / Invoice line item ID',
    material_id BIGINT COMMENT '物料ID / Material ID',
    material_code VARCHAR(64) COMMENT '物料编码 / Material code',
    po_qty DECIMAL(14,4) COMMENT '订单数量 / PO quantity',
    receive_qty DECIMAL(14,4) COMMENT '收货数量 / Received quantity',
    invoice_qty DECIMAL(14,4) COMMENT '发票数量 / Invoice quantity',
    po_unit_price DECIMAL(14,4) COMMENT '订单单价 / PO unit price',
    invoice_unit_price DECIMAL(14,4) COMMENT '发票单价 / Invoice unit price',
    match_status VARCHAR(32) NOT NULL DEFAULT 'UNMATCHED' COMMENT '匹配状态: UNMATCHED未匹配/MATCHED已匹配/PARTIAL部分匹配/DISCREPANCY差异 /
        Match status: UNMATCHED/MATCHED/PARTIAL/DISCREPANCY',
    qty_discrepancy DECIMAL(14,4) COMMENT '数量差异 / Quantity discrepancy',
    price_discrepancy DECIMAL(14,4) COMMENT '价格差异 / Price discrepancy',
    tolerance_qty_pct DECIMAL(5,2) DEFAULT 5.00 COMMENT '数量容差百分比 / Quantity tolerance percentage',
    tolerance_price_pct DECIMAL(5,2) DEFAULT 2.00 COMMENT '价格容差百分比 / Price tolerance percentage',
    resolution VARCHAR(32) COMMENT '差异处理方式: WAIT_MANUAL等待人工/CONCESSION让步接收/ADJUST调整/REJECTED拒绝 /
        Resolution: WAIT_MANUAL/CONCESSION/ADJUST/REJECTED',
    resolution_by BIGINT COMMENT '处理人 / Resolved by',
    resolution_at DATETIME COMMENT '处理时间 / Resolution time',
    resolution_note VARCHAR(500) COMMENT '处理备注 / Resolution note',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_po_receive_invoice_line (po_line_id, receive_line_id, invoice_line_id),
    INDEX idx_po_id (po_id),
    INDEX idx_match_status (match_status),
    INDEX idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='三单匹配表 / Three-way match table';

-- P0-3.7.4 对账单主表 / Settlement reconciliation main table
CREATE TABLE IF NOT EXISTS settlement_recon (
    recon_id BIGINT PRIMARY KEY COMMENT '对账单ID / Reconciliation ID',
    recon_no VARCHAR(64) NOT NULL COMMENT '对账单号 / Reconciliation number',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    supplier_name VARCHAR(128) COMMENT '供应商名称 / Supplier name',
    period_start DATE NOT NULL COMMENT '对账周期起始 / Period start',
    period_end DATE NOT NULL COMMENT '对账周期截止 / Period end',
    recon_amount DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '系统对账金额 / System reconciliation amount',
    confirmed_amount DECIMAL(14,2) COMMENT '供应商确认金额 / Supplier confirmed amount',
    diff_amount DECIMAL(14,2) DEFAULT 0 COMMENT '差异总金额 / Total difference amount',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态: DRAFT草稿/SENT已发送/SUPPLIER_PENDING供应商待确认/
        DISPUTED有异议/CONFIRMED已确认/RESOLVED已解决 / Status',
    sent_at DATETIME COMMENT '发送时间 / Sent time',
    supplier_confirmed_at DATETIME COMMENT '供应商确认时间 / Supplier confirmed time',
    resolved_at DATETIME COMMENT '解决时间 / Resolved time',
    created_by BIGINT COMMENT '创建人 / Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_recon_no (recon_no),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_period (period_start, period_end),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商对账单表 / Supplier reconciliation table';

-- P0-3.7.4 对账单差异明细表 / Reconciliation discrepancy detail table
CREATE TABLE IF NOT EXISTS settlement_recon_detail (
    id BIGINT PRIMARY KEY COMMENT '主键ID / Primary key',
    recon_id BIGINT NOT NULL COMMENT '对账单ID / Reconciliation ID',
    item_type VARCHAR(32) NOT NULL COMMENT '差异项类型: QTY_DIFF数量差异/PRICE_DIFF价格差异/
        MISSING_RECEIPT缺收/MISSING_RETURN缺退/OTHER其他 / Discrepancy type',
    reference_no VARCHAR(128) COMMENT '关联单号 / Reference number',
    system_qty DECIMAL(14,4) COMMENT '系统数量 / System quantity',
    supplier_qty DECIMAL(14,4) COMMENT '供应商数量 / Supplier quantity',
    system_amount DECIMAL(14,2) COMMENT '系统金额 / System amount',
    supplier_amount DECIMAL(14,2) COMMENT '供应商金额 / Supplier amount',
    diff_amount DECIMAL(14,2) COMMENT '差异金额 / Difference amount',
    status VARCHAR(32) DEFAULT 'OPEN' COMMENT '状态: OPEN未解决/CONFIRMED确认/DISPUTED申诉/
        RESOLVED已解决 / Status',
    supplier_feedback TEXT COMMENT '供应商反馈 / Supplier feedback',
    resolved_by BIGINT COMMENT '解决人 / Resolved by',
    resolved_at DATETIME COMMENT '解决时间 / Resolved time',
    note VARCHAR(500) COMMENT '备注 / Note',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_recon_id (recon_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='对账单差异明细 / Reconciliation discrepancy detail';

-- P1-3.7.5 账龄分析快照表 / Aging analysis snapshot table
CREATE TABLE IF NOT EXISTS aging_analysis (
    aging_id BIGINT PRIMARY KEY COMMENT '账龄记录ID / Aging record ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    supplier_name VARCHAR(128) COMMENT '供应商名称 / Supplier name',
    as_of_date DATE NOT NULL COMMENT '账龄截止日期 / As-of date',
    total_payable DECIMAL(14,2) NOT NULL DEFAULT 0 COMMENT '应付总金额 / Total payable amount',
    aging_0_30 DECIMAL(14,2) DEFAULT 0 COMMENT '0-30天 / 0-30 days',
    aging_31_60 DECIMAL(14,2) DEFAULT 0 COMMENT '31-60天 / 31-60 days',
    aging_61_90 DECIMAL(14,2) DEFAULT 0 COMMENT '61-90天 / 61-90 days',
    aging_91_plus DECIMAL(14,2) DEFAULT 0 COMMENT '91天以上 / 91+ days',
    overdue_flag TINYINT DEFAULT 0 COMMENT '超90天预警标记 / Over 90-day alert flag: 0正常 1预警',
    calculated_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '计算时间 / Calculation time',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_as_of_date (as_of_date),
    INDEX idx_overdue_flag (overdue_flag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='应付账款账龄分析表 / Accounts payable aging analysis table';

-- ============================================================
-- 3.8 合同管理 / Contract Management
-- ============================================================

-- P0-3.8.1 合同条款库 / Contract clause library
CREATE TABLE IF NOT EXISTS contract_clause (
    clause_id BIGINT PRIMARY KEY COMMENT '条款ID / Clause ID',
    clause_code VARCHAR(64) NOT NULL COMMENT '条款编码 / Clause code',
    category VARCHAR(32) NOT NULL COMMENT '条款分类: LAW法律/COMMERCIAL商务/STANDARD通用 /
        Clause category: LAW/COMMERCIAL/STANDARD',
    title VARCHAR(255) NOT NULL COMMENT '条款标题 / Clause title',
    content TEXT NOT NULL COMMENT '条款内容 / Clause content',
    version INT DEFAULT 1 COMMENT '版本号 / Version number',
    effective_date DATE COMMENT '生效日期 / Effective date',
    status VARCHAR(32) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE生效/INACTIVE失效/ARCHIVED归档 / Status',
    created_by BIGINT COMMENT '创建人 / Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_clause_code_version (clause_code, version),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同条款库 / Contract clause library';

-- P1-3.8.2 合同类型与关联扩展 / Contract type and linkage extension
ALTER TABLE contract
    ADD COLUMN IF NOT EXISTS contract_type VARCHAR(32) DEFAULT 'STANDALONE' COMMENT '合同类型: FRAMEWORK框架/EXECUTION执行/STANDALONE独立 /
        Contract type: FRAMEWORK/EXECUTION/STANDALONE',
    ADD COLUMN IF NOT EXISTS parent_contract_id BIGINT COMMENT '父合同ID（执行合同关联框架合同）/ Parent contract ID (execution links to framework)',
    ADD COLUMN IF NOT EXISTS framework_max_amount DECIMAL(14,2) COMMENT '框架合同金额上限 / Framework max amount',
    ADD COLUMN IF NOT EXISTS framework_max_qty DECIMAL(14,2) COMMENT '框架合同数量上限 / Framework max quantity',
    ADD COLUMN IF NOT EXISTS framework_consumed_amount DECIMAL(14,2) DEFAULT 0 COMMENT '已执行金额 / Consumed amount',
    ADD COLUMN IF NOT EXISTS framework_consumed_qty DECIMAL(14,2) DEFAULT 0 COMMENT '已执行数量 / Consumed quantity',
    ADD COLUMN IF NOT EXISTS auto_renewal TINYINT DEFAULT 0 COMMENT '自动续约标记 / Auto-renewal flag: 0否 1是',
    ADD COLUMN IF NOT EXISTS renewal_notice_days INT DEFAULT 30 COMMENT '续约提前通知天数 / Renewal notice days',
    ADD COLUMN IF NOT EXISTS expired_at DATE COMMENT '合同到期日期（冗余，加速查询）/ Expiry date (redundant for query speed)',
    ADD INDEX IF NOT EXISTS idx_contract_type (contract_type),
    ADD INDEX IF NOT EXISTS idx_parent_contract (parent_contract_id),
    ADD INDEX IF NOT EXISTS idx_expired_at (expired_at),
    ADD INDEX IF NOT EXISTS idx_auto_renewal (auto_renewal);

-- P0-3.8.4 合同到期提醒记录表 / Contract expiry alert record table
CREATE TABLE IF NOT EXISTS contract_alert (
    alert_id BIGINT PRIMARY KEY COMMENT '提醒ID / Alert ID',
    contract_id BIGINT NOT NULL COMMENT '合同ID / Contract ID',
    contract_no VARCHAR(64) COMMENT '合同编号 / Contract number',
    alert_type VARCHAR(32) NOT NULL COMMENT '提醒类型: EXPIRY_90到期前90天/EXPIRY_60/EXPIRY_30/EXPIRY_7/EXPIRY_1/EXPIRED已到期 /
        Alert type: EXPIRY_90/EXPIRY_60/EXPIRY_30/EXPIRY_7/EXPIRY_1/EXPIRED',
    expire_date DATE NOT NULL COMMENT '到期日 / Expiry date',
    alert_date DATE NOT NULL COMMENT '提醒日期 / Alert date',
    notified_owner TINYINT DEFAULT 0 COMMENT '已通知合同负责人 / Owner notified',
    notified_supplier TINYINT DEFAULT 0 COMMENT '已通知供应商 / Supplier notified',
    status VARCHAR(32) DEFAULT 'PENDING' COMMENT '状态: PENDING待发送/SENT已发送/READ已读 / Status',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    INDEX idx_contract_id (contract_id),
    INDEX idx_alert_date (alert_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同到期提醒表 / Contract expiry alert table';

-- ============================================================
-- 3.9 消息中心 / Message Center
-- ============================================================

-- P0-3.9.1 消息优先级字段扩展 / Message priority field extension
ALTER TABLE msg_record
    ADD COLUMN IF NOT EXISTS priority INT DEFAULT 2 COMMENT '消息优先级: 1紧急 2重要 3普通 / Priority: 1-Urgent 2-Important 3-Normal',
    ADD INDEX IF NOT EXISTS idx_priority (priority);

-- P1-3.9.2 已读时间字段（已存在于read_at，此处补充批量操作索引）/ read_time exists, add batch operation indexes
ALTER TABLE msg_record
    ADD INDEX IF NOT EXISTS idx_is_read_supplier (is_read, supplier_id);

-- P1-3.9.5 消息渠道偏好配置表 / Message channel preference table
CREATE TABLE IF NOT EXISTS msg_channel_preference (
    pref_id BIGINT PRIMARY KEY COMMENT '偏好ID / Preference ID',
    user_id BIGINT NOT NULL COMMENT '用户ID / User ID',
    event_type VARCHAR(32) NOT NULL COMMENT '消息事件类型: ORDER/DELIVERY/SETTLEMENT/SYSTEM/APPROVAL/QUALITY /
        Event type',
    ws_enabled TINYINT DEFAULT 1 COMMENT 'WebSocket通道启用 / WebSocket enabled',
    mail_enabled TINYINT DEFAULT 0 COMMENT '邮件通道启用 / Mail enabled',
    sms_enabled TINYINT DEFAULT 0 COMMENT '短信通道启用 / SMS enabled',
    quiet_start TIME COMMENT '静默时段起始 / Quiet hours start',
    quiet_end TIME COMMENT '静默时段截止 / Quiet hours end',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_user_event (user_id, event_type),
    INDEX idx_user_id (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消息渠道偏好配置 / Message channel preference';

-- ============================================================
-- 3.10 系统公共 / System Common
-- ============================================================

-- P0-3.10.3 审批流模板表 / Approval flow template table
CREATE TABLE IF NOT EXISTS approval_template (
    template_id BIGINT PRIMARY KEY COMMENT '模板ID / Template ID',
    name VARCHAR(128) NOT NULL COMMENT '模板名称 / Template name',
    module VARCHAR(64) NOT NULL COMMENT '所属模块: SUPPLIER_ONBOARDING供应商准入/ORDER_CHANGE订单变更/
        CONTRACT_SIGNING合同签署/PAYMENT_APPROVAL付款审批 /
        Module: SUPPLIER_ONBOARDING/ORDER_CHANGE/CONTRACT_SIGNING/PAYMENT_APPROVAL',
    steps JSON NOT NULL COMMENT '审批步骤: [{"step":1,"role":"DEPT_MGR"},{"step":2,"role":"FINANCE"}] /
        Approval steps',
    is_default TINYINT DEFAULT 0 COMMENT '是否默认模板 / Is default template',
    status VARCHAR(32) DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE生效/INACTIVE失效 / Status',
    created_by BIGINT COMMENT '创建人 / Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_module_name (module, name),
    INDEX idx_module (module),
    INDEX idx_is_default (is_default)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='审批流模板表 / Approval flow template table';

-- P1-3.10.2 组织架构树形表 / Organization structure tree table
CREATE TABLE IF NOT EXISTS org_structure (
    node_id BIGINT PRIMARY KEY COMMENT '组织节点ID / Org node ID',
    parent_id BIGINT COMMENT '父节点ID / Parent node ID',
    node_type VARCHAR(32) NOT NULL COMMENT '节点类型: GROUP集团/DIVISION事业部/PLANT工厂/WORKSHOP车间/
        LINE产线 / Node type: GROUP/DIVISION/PLANT/WORKSHOP/LINE',
    name VARCHAR(128) NOT NULL COMMENT '节点名称 / Node name',
    code VARCHAR(64) NOT NULL COMMENT '节点编码 / Node code',
    node_path VARCHAR(500) COMMENT '节点路径（如 /1/2/5/）/ Node path (e.g., /1/2/5/)',
    node_level INT COMMENT '节点层级 / Node level',
    sort_order INT DEFAULT 0 COMMENT '排序 / Sort order',
    manager_id BIGINT COMMENT '负责人ID / Manager ID',
    status TINYINT DEFAULT 1 COMMENT '状态: 1启用 0停用 / Status: 1-active 0-inactive',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_code (code),
    INDEX idx_parent_id (parent_id),
    INDEX idx_node_type (node_type),
    INDEX idx_node_path (node_path(200))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织架构树形表 / Organization structure tree table';

-- P1-3.10.4 数据导出模板表 / Export template table
CREATE TABLE IF NOT EXISTS export_template (
    template_id BIGINT PRIMARY KEY COMMENT '模板ID / Template ID',
    name VARCHAR(128) NOT NULL COMMENT '模板名称 / Template name',
    module VARCHAR(64) NOT NULL COMMENT '所属模块: SUPPLIER供应商/ORDER订单/SETTLEMENT结算/
        QUALITY质量/PERFORMANCE绩效 / Module',
    columns JSON NOT NULL COMMENT '导出列定义: [{"field":"supplierName","label":"供应商名称","width":20}] /
        Export column definitions',
    creator BIGINT COMMENT '创建人 / Creator ID',
    creator_name VARCHAR(128) COMMENT '创建人姓名 / Creator name',
    is_shared TINYINT DEFAULT 0 COMMENT '是否部门共享 / Is department-shared',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_module (module),
    INDEX idx_creator (creator)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='数据导出模板表 / Export template table';

-- ============================================================
-- P0-3.10.3 审批流模板预置数据 / Approval template preset data
-- ============================================================
INSERT IGNORE INTO approval_template (template_id, name, module, steps, is_default, status) VALUES
(100001, '供应商准入审批 / Supplier Onboarding Approval', 'SUPPLIER_ONBOARDING',
 '[{"step":1,"role":"DEPT_MGR"},{"step":2,"role":"FINANCE"},{"step":3,"role":"GM"}]', 1, 'ACTIVE'),
(100002, '订单变更审批 / Order Change Approval', 'ORDER_CHANGE',
 '[{"step":1,"role":"DEPT_MGR"},{"step":2,"role":"FINANCE"}]', 1, 'ACTIVE'),
(100003, '合同签署审批 / Contract Signing Approval', 'CONTRACT_SIGNING',
 '[{"step":1,"role":"DEPT_MGR"},{"step":2,"role":"LEGAL"},{"step":3,"role":"GM"}]', 1, 'ACTIVE'),
(100004, '付款审批 / Payment Approval', 'PAYMENT_APPROVAL',
 '[{"step":1,"role":"FINANCE"},{"step":2,"role":"CFO"}]', 1, 'ACTIVE');
