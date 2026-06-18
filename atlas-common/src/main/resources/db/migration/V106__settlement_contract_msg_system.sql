-- =============================================================================
-- V106__settlement_contract_msg_system.sql
-- 结算管理 / 合同增强 / 消息系统扩展 / 组织架构 / Settlement / Contract / Message / Org
-- 日期: 2026-06-18 / Date: 2026-06-18
-- =============================================================================

-- -------------------------------------------------------------------------
-- 1. 三单匹配 / Three-Way Match (PO + Receiving + Invoice)
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS three_way_match (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    match_no        VARCHAR(64)   NOT NULL                             COMMENT '匹配单号 / Match number',
    purchase_order_id BIGINT      NOT NULL                             COMMENT '采购订单ID / Purchase order ID',
    receiving_id    BIGINT        NOT NULL                             COMMENT '收货记录ID / Receiving record ID',
    invoice_id      BIGINT                                              COMMENT '发票ID / Invoice ID',
    po_amount       DECIMAL(18,2) NOT NULL                             COMMENT '订单金额 / PO amount',
    received_amount DECIMAL(18,2) NOT NULL                             COMMENT '收货金额 / Received amount',
    invoice_amount  DECIMAL(18,2)                                      COMMENT '发票金额 / Invoice amount',
    qty_match       TINYINT(1)    NOT NULL DEFAULT 0                   COMMENT '数量匹配(1=一致 0=不一致) / Quantity match',
    price_match     TINYINT(1)    NOT NULL DEFAULT 0                   COMMENT '价格匹配(1=一致 0=不一致) / Price match',
    match_status    VARCHAR(16)   NOT NULL DEFAULT 'PENDING'           COMMENT '匹配状态(PENDING/MATCHED/MISMATCHED) / Match status',
    mismatch_reason VARCHAR(512)                                       COMMENT '不匹配原因 / Mismatch reason',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_twm_no (match_no),
    INDEX idx_twm_order (purchase_order_id),
    INDEX idx_twm_status (match_status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='三单匹配 / Three-way match';

-- -------------------------------------------------------------------------
-- 2. 对账单主表 / Settlement Reconciliation Master
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS settlement_recon (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    recon_no        VARCHAR(64)   NOT NULL                             COMMENT '对账单号 / Reconciliation number',
    supplier_id     BIGINT        NOT NULL                             COMMENT '供应商ID / Supplier ID',
    period_start    DATE          NOT NULL                             COMMENT '账期起始 / Period start',
    period_end      DATE          NOT NULL                             COMMENT '账期截止 / Period end',
    total_amount    DECIMAL(18,2) NOT NULL DEFAULT 0                   COMMENT '对账总金额 / Total amount',
    matched_amount  DECIMAL(18,2) NOT NULL DEFAULT 0                   COMMENT '匹配金额 / Matched amount',
    diff_amount     DECIMAL(18,2) NOT NULL DEFAULT 0                   COMMENT '差异金额 / Difference amount',
    status          VARCHAR(16)   NOT NULL DEFAULT 'DRAFT'             COMMENT '状态(DRAFT/CONFIRMED/DISPUTED/SETTLED) / Status',
    confirmed_by    BIGINT                                              COMMENT '确认人ID / Confirmed by',
    confirmed_at    DATETIME                                            COMMENT '确认时间 / Confirmed time',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_recon_no (recon_no),
    INDEX idx_sr_supplier (supplier_id),
    INDEX idx_sr_period (period_start, period_end)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='对账单主表 / Settlement reconciliation master';

-- -------------------------------------------------------------------------
-- 3. 对账单明细 / Settlement Reconciliation Detail
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS settlement_recon_detail (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    recon_id        BIGINT        NOT NULL                             COMMENT '对账单ID / Reconciliation ID',
    biz_type        VARCHAR(16)   NOT NULL                             COMMENT '业务类型(PO/RECEIVING/RETURN) / Business type',
    biz_id          BIGINT        NOT NULL                             COMMENT '业务单据ID / Business document ID',
    biz_no          VARCHAR(64)   NOT NULL                             COMMENT '业务单号 / Business document number',
    amount          DECIMAL(18,2) NOT NULL                             COMMENT '金额 / Amount',
    matched         TINYINT(1)    NOT NULL DEFAULT 0                   COMMENT '是否已匹配 / Matched',
    remark          VARCHAR(512)                                       COMMENT '备注 / Remark',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    PRIMARY KEY (id),
    INDEX idx_srd_recon (recon_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='对账单明细 / Settlement reconciliation detail';

-- -------------------------------------------------------------------------
-- 4. 合同条款 / Contract Clause
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS contract_clause (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    contract_id     BIGINT        NOT NULL                             COMMENT '合同ID / Contract ID',
    clause_code     VARCHAR(32)   NOT NULL                             COMMENT '条款编码 / Clause code',
    clause_title    VARCHAR(256)  NOT NULL                             COMMENT '条款标题 / Clause title',
    clause_content  TEXT          NOT NULL                             COMMENT '条款内容 / Clause content',
    sort_order      INT          DEFAULT 0                             COMMENT '排序序号 / Sort order',
    is_required     TINYINT(1)    NOT NULL DEFAULT 0                   COMMENT '是否必选 / Is required',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    INDEX idx_cc_contract (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='合同条款 / Contract clause';

-- -------------------------------------------------------------------------
-- 5. 合同预警 / Contract Alert
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS contract_alert (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    contract_id     BIGINT        NOT NULL                             COMMENT '合同ID / Contract ID',
    alert_type      VARCHAR(16)   NOT NULL                             COMMENT '预警类型(EXPIRE/AMOUNT/PERFORMANCE) / Alert type',
    alert_rule      VARCHAR(128)  NOT NULL                             COMMENT '预警规则(如 <30天) / Alert rule',
    alert_value     VARCHAR(64)   NOT NULL                             COMMENT '预警阈值 / Alert threshold',
    is_enabled      TINYINT(1)    NOT NULL DEFAULT 1                   COMMENT '是否启用 / Is enabled',
    last_triggered  DATETIME                                            COMMENT '最近触发时间 / Last triggered time',
    notify_users    VARCHAR(512)                                       COMMENT '通知用户ID列表(逗号分隔) / Notify user IDs',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    INDEX idx_ca_contract (contract_id),
    INDEX idx_ca_type (alert_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='合同预警 / Contract alert';

-- -------------------------------------------------------------------------
-- 6. 合同表扩展 / Contract Extension
-- -------------------------------------------------------------------------
ALTER TABLE contract
    ADD COLUMN IF NOT EXISTS contract_type       VARCHAR(16)  COMMENT '合同类型(MASTER/SUPPLEMENT/FRAME) / Contract type',
    ADD COLUMN IF NOT EXISTS parent_contract_id  BIGINT       COMMENT '父合同ID(补充协议场景) / Parent contract ID',
    ADD COLUMN IF NOT EXISTS amount              DECIMAL(18,2) COMMENT '合同金额 / Contract amount';

-- -------------------------------------------------------------------------
-- 7. 消息渠道偏好 / Message Channel Preference
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS msg_channel_preference (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    user_id         BIGINT        NOT NULL                             COMMENT '用户ID / User ID',
    event_type      VARCHAR(32)   NOT NULL                             COMMENT '事件类型 / Event type',
    channel         VARCHAR(16)   NOT NULL                             COMMENT '推送渠道(WEBSOCKET/MAIL/SMS) / Push channel',
    is_enabled      TINYINT(1)    NOT NULL DEFAULT 1                   COMMENT '是否启用 / Is enabled',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_mcp_user_event_channel (user_id, event_type, channel)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='消息渠道偏好 / Message channel preference';

-- -------------------------------------------------------------------------
-- 8. 审批模板 / Approval Template
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS approval_template (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    template_code   VARCHAR(64)   NOT NULL                             COMMENT '模板编码 / Template code',
    template_name   VARCHAR(128)  NOT NULL                             COMMENT '模板名称 / Template name',
    biz_type        VARCHAR(32)   NOT NULL                             COMMENT '业务类型(ORDER_CHANGE/PPAP/NCR等) / Business type',
    approval_chain  TEXT          NOT NULL                             COMMENT '审批链JSON(多级审批人/角色) / Approval chain JSON',
    is_active       TINYINT(1)    NOT NULL DEFAULT 1                   COMMENT '是否启用 / Is active',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_at_code (template_code),
    INDEX idx_at_biz_type (biz_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='审批模板 / Approval template';

-- -------------------------------------------------------------------------
-- 9. 组织架构 / Organization Structure
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS org_structure (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    org_code        VARCHAR(32)   NOT NULL                             COMMENT '组织编码 / Org code',
    org_name        VARCHAR(128)  NOT NULL                             COMMENT '组织名称 / Org name',
    parent_id       BIGINT        DEFAULT 0                            COMMENT '父组织ID，0为顶级 / Parent org ID, 0=top',
    org_type        VARCHAR(16)   NOT NULL                             COMMENT '组织类型(COMPANY/DEPT/TEAM) / Org type',
    manager_id      BIGINT                                              COMMENT '负责人ID / Manager ID',
    sort_order      INT          DEFAULT 0                             COMMENT '排序序号 / Sort order',
    status          VARCHAR(16)   NOT NULL DEFAULT 'ACTIVE'            COMMENT '状态(ACTIVE/INACTIVE) / Status',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_org_code (org_code),
    INDEX idx_org_parent (parent_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='组织架构 / Organization structure';

-- -------------------------------------------------------------------------
-- 10. 导出模板 / Export Template
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS export_template (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    template_code   VARCHAR(64)   NOT NULL                             COMMENT '模板编码 / Template code',
    template_name   VARCHAR(128)  NOT NULL                             COMMENT '模板名称 / Template name',
    biz_type        VARCHAR(32)   NOT NULL                             COMMENT '业务类型(ORDER/RECEIVING/INVENTORY等) / Business type',
    columns_config  TEXT          NOT NULL                             COMMENT '列配置JSON(字段/标题/顺序/宽度) / Column config JSON',
    filter_config   TEXT                                                COMMENT '筛选条件JSON / Filter config JSON',
    is_system       TINYINT(1)    NOT NULL DEFAULT 0                   COMMENT '是否系统预置 / Is system preset',
    created_by      BIGINT                                              COMMENT '创建人ID / Created by',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_et_code (template_code),
    INDEX idx_et_biz_type (biz_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='导出模板 / Export template';

-- -------------------------------------------------------------------------
-- 11. 消息记录表扩展 / Message Record Extension
-- -------------------------------------------------------------------------
ALTER TABLE msg_record
    ADD COLUMN IF NOT EXISTS priority  VARCHAR(8)   DEFAULT 'NORMAL' COMMENT '消息优先级(HIGH/NORMAL/LOW) / Message priority',
    ADD COLUMN IF NOT EXISTS read_time DATETIME                     COMMENT '已读时间 / Read time';
