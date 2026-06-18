-- ============================================
-- Atlas 电子合同管理模块 DDL
-- Atlas Electronic Contract Management Module DDL
-- Version: V97
-- ============================================

-- 合同模板表 / Contract Template Table
CREATE TABLE cnt_template (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT  COMMENT '主键 / Primary Key',
    template_name   VARCHAR(200)  NOT NULL              COMMENT '模板名称 / Template name',
    template_code   VARCHAR(50)   UNIQUE NOT NULL       COMMENT '模板编码 / Template code',
    category        VARCHAR(50)   NOT NULL              COMMENT '模板分类: PURCHASE(采购)/SERVICE(服务)/NDA(保密)/FRAMEWORK(框架)',
    description     TEXT                                COMMENT '模板描述 / Template description',
    file_path       VARCHAR(500)                        COMMENT '模板文件路径(.docx/.pdf) / Template file path',
    version         VARCHAR(20)   DEFAULT '1.0'         COMMENT '模板版本号 / Template version',
    is_active       TINYINT       DEFAULT 1             COMMENT '是否启用: 0-禁用 1-启用 / Active: 0-disabled 1-enabled',
    created_by      VARCHAR(100)                        COMMENT '创建人 / Created by',
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间 / Created at',
    updated_at      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated at'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同模板表 / Contract Template Table';

-- 合同签署流程表 / Contract Sign Flow Table
CREATE TABLE cnt_sign_flow (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT  COMMENT '主键 / Primary Key',
    contract_id     BIGINT        NOT NULL              COMMENT '关联合同ID / Contract ID',
    sign_type       VARCHAR(20)   NOT NULL              COMMENT '签署类型: ONLINE(在线签署)/OFFLINE(线下签署)',
    status          VARCHAR(20)   DEFAULT 'DRAFT'       COMMENT '签署状态: DRAFT(草稿)/SIGNING(签署中)/COMPLETED(已完成)/EXPIRED(已过期)/CANCELLED(已取消)',
    initiator_id    BIGINT        NOT NULL              COMMENT '发起人ID / Initiator ID',
    current_step    INT           DEFAULT 1             COMMENT '当前步骤 / Current step',
    total_steps     INT           DEFAULT 2             COMMENT '总步骤数 / Total steps',
    sign_deadline   DATETIME                            COMMENT '签署截止时间 / Sign deadline',
    completed_at    DATETIME                            COMMENT '完成时间 / Completed at',
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间 / Created at',
    updated_at      DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated at',
    INDEX idx_contract_id (contract_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='合同签署流程表 / Contract Sign Flow Table';

-- 签署记录表 / Sign Record Table
CREATE TABLE cnt_sign_record (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT  COMMENT '主键 / Primary Key',
    flow_id         BIGINT        NOT NULL              COMMENT '签署流程ID / Sign flow ID',
    step_order      INT           NOT NULL              COMMENT '第几步签署 / Step order',
    signer_type     VARCHAR(20)   NOT NULL              COMMENT '签署人类型: INTERNAL(内部)/EXTERNAL(外部)',
    signer_id       BIGINT        NOT NULL              COMMENT '签署人ID / Signer ID',
    signer_name     VARCHAR(100)                        COMMENT '签署人姓名 / Signer name',
    signer_company  VARCHAR(200)                        COMMENT '签署人公司 / Signer company',
    sign_status     VARCHAR(20)   DEFAULT 'PENDING'     COMMENT '签署状态: PENDING(待签)/SIGNED(已签)/REJECTED(拒签)',
    signed_at       DATETIME                            COMMENT '签署时间 / Signed at',
    sign_ip         VARCHAR(50)                         COMMENT '签署IP / Sign IP',
    sign_method     VARCHAR(20)                         COMMENT '签署方式: SMS_VERIFY(短信验证)/SEAL_IMAGE(印章图片)/HANDWRITE(手写签名)/THIRD_PARTY(第三方)',
    reject_reason   VARCHAR(500)                        COMMENT '拒签原因 / Reject reason',
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间 / Created at',
    INDEX idx_flow_id (flow_id),
    INDEX idx_signer_id (signer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='签署记录表 / Sign Record Table';

-- 条款比对记录表 / Clause Compare Record Table
CREATE TABLE cnt_clause_compare (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT  COMMENT '主键 / Primary Key',
    contract_id     BIGINT        NOT NULL              COMMENT '合同ID / Contract ID',
    source_version  VARCHAR(50)                         COMMENT '原始版本号 / Source version',
    target_version  VARCHAR(50)                         COMMENT '对比目标版本号 / Target version',
    diff_result     TEXT                                COMMENT 'Diff结果(JSON格式) / Diff result (JSON)',
    compared_by     VARCHAR(100)                        COMMENT '比对人 / Compared by',
    compared_at     DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '比对时间 / Compared at',
    INDEX idx_contract_id (contract_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='条款比对记录表 / Clause Compare Record Table';

-- 履约跟踪表 / Contract Performance Tracking Table
CREATE TABLE cnt_performance (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT  COMMENT '主键 / Primary Key',
    contract_id     BIGINT        NOT NULL              COMMENT '合同ID / Contract ID',
    clause_ref      VARCHAR(100)                        COMMENT '条款引用(如 第3.2条) / Clause reference (e.g. Art 3.2)',
    metric_name     VARCHAR(200)                        COMMENT '履约指标名称 / Performance metric name',
    metric_type     VARCHAR(50)                         COMMENT '指标类型: DELIVERY(交付)/PAYMENT(付款)/QUALITY(质量)/MILESTONE(里程碑)',
    target_value    VARCHAR(200)                        COMMENT '目标值 / Target value',
    actual_value    VARCHAR(200)                        COMMENT '实际值 / Actual value',
    status          VARCHAR(20)   DEFAULT 'NOT_STARTED' COMMENT '履约状态: NOT_STARTED(未开始)/IN_PROGRESS(进行中)/COMPLETED(已完成)/BREACHED(已违约)',
    due_date        DATE                                COMMENT '到期日期 / Due date',
    completed_at    DATETIME                            COMMENT '完成时间 / Completed at',
    remark          TEXT                                COMMENT '备注 / Remark',
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间 / Created at',
    INDEX idx_contract_id (contract_id),
    INDEX idx_status (status),
    INDEX idx_due_date (due_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='履约跟踪表 / Contract Performance Tracking Table';

-- 履约提醒表 / Performance Alert Table
CREATE TABLE cnt_performance_alert (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT  COMMENT '主键 / Primary Key',
    performance_id  BIGINT        NOT NULL              COMMENT '履约指标ID / Performance ID',
    alert_type      VARCHAR(30)                         COMMENT '提醒类型: DUE_SOON(即将到期)/OVERDUE(已逾期)/BREACH(违约)',
    alert_message   VARCHAR(500)                        COMMENT '提醒消息 / Alert message',
    notify_to       VARCHAR(200)                        COMMENT '通知对象 / Notify target',
    is_sent         TINYINT       DEFAULT 0             COMMENT '是否已发送: 0-未发送 1-已发送 / Sent: 0-no 1-yes',
    sent_at         DATETIME                            COMMENT '发送时间 / Sent at',
    created_at      DATETIME      DEFAULT CURRENT_TIMESTAMP  COMMENT '创建时间 / Created at',
    INDEX idx_performance_id (performance_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='履约提醒表 / Performance Alert Table';
