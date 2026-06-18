-- ============================================
-- Atlas V7: Flowable 工作流引擎初始化
-- ============================================

CREATE DATABASE IF NOT EXISTS atlas_flowable DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE atlas_flowable;

-- 业务工作流实例关联表
CREATE TABLE IF NOT EXISTS biz_workflow (
    id              BIGINT PRIMARY KEY,
    biz_type        VARCHAR(30) NOT NULL COMMENT '业务类型(PURCHASE_APPROVAL/CONTRACT_APPROVAL)',
    biz_id          BIGINT NOT NULL COMMENT '业务主键(采购单ID/合同ID)',
    process_def_key VARCHAR(100) NOT NULL COMMENT '流程定义Key',
    process_inst_id VARCHAR(64) COMMENT '流程实例ID(Flowable生成)',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '0进行中 1通过 2驳回 3撤回',
    initiator_id    BIGINT NOT NULL COMMENT '发起人',
    current_node    VARCHAR(50) COMMENT '当前审批节点',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_biz (biz_type, biz_id),
    KEY idx_process_inst (process_inst_id),
    KEY idx_initiator (initiator_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='业务工作流关联表';
