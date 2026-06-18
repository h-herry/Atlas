-- ============================================
-- Atlas V3: 合同管理
-- ============================================

CREATE DATABASE IF NOT EXISTS atlas_contract DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE atlas_contract;

CREATE TABLE IF NOT EXISTS contract (
    id              BIGINT PRIMARY KEY COMMENT '合同ID',
    contract_no     VARCHAR(32) NOT NULL COMMENT '合同编号',
    supplier_id     BIGINT NOT NULL COMMENT '供应商ID',
    title           VARCHAR(200) NOT NULL COMMENT '合同标题',
    contract_type   TINYINT NOT NULL COMMENT '1采购合同 2框架协议 3补充协议',
    total_amount    DECIMAL(18,2) NOT NULL COMMENT '合同总金额',
    signed_amount   DECIMAL(18,2) NOT NULL DEFAULT 0 COMMENT '已签约金额',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '状态见状态机',
    effective_date  DATE COMMENT '生效日期',
    expire_date     DATE COMMENT '到期日期',
    payment_terms   TEXT COMMENT '付款条款(JSON)',
    attachment_urls TEXT COMMENT '附件列表(JSON)',
    created_by      BIGINT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_contract_no (contract_no),
    KEY idx_supplier_status (supplier_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同表';

CREATE TABLE IF NOT EXISTS contract_change_log (
    id              BIGINT PRIMARY KEY,
    contract_id     BIGINT NOT NULL,
    change_type     TINYINT NOT NULL COMMENT '1金额变更 2条款变更 3终止 4续签',
    before_snapshot JSON COMMENT '变更前快照',
    after_snapshot  JSON COMMENT '变更后快照',
    change_reason   VARCHAR(500) COMMENT '变更原因',
    changed_by      BIGINT NOT NULL,
    changed_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_contract_id (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同变更记录表';

CREATE TABLE IF NOT EXISTS contract_approval (
    id              BIGINT PRIMARY KEY,
    contract_id     BIGINT NOT NULL,
    approval_node   VARCHAR(50) NOT NULL COMMENT '审批节点',
    approver_id     BIGINT NOT NULL,
    result          TINYINT COMMENT '1通过 2驳回 3转审',
    comment         VARCHAR(500) COMMENT '审批意见',
    approved_at     DATETIME,
    KEY idx_contract_id (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同审批记录表';
