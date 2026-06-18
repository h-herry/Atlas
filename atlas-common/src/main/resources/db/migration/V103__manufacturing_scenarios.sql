-- =============================================================================
-- V103__manufacturing_scenarios.sql
-- 制造业场景扩展 / Manufacturing Scenario Extension
-- 日期: 2026-06-18 / Date: 2026-06-18
-- 涵盖: JIT交货排程 / VMI库存监控 / PPAP提交 / 供应商工厂关联 / 来料批次追溯
-- Covers: JIT delivery schedule / VMI inventory monitoring / PPAP submission /
--         supplier-plant relation / lot traceability
-- =============================================================================

-- -------------------------------------------------------------------------
-- 1. JIT交货排程 / JIT Delivery Schedule
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS jit_delivery_schedule (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    purchase_order_id BIGINT      NOT NULL                             COMMENT '采购订单ID / Purchase order ID',
    material_code   VARCHAR(64)   NOT NULL                             COMMENT '物料编码 / Material code',
    demand_quantity DECIMAL(18,4) NOT NULL                             COMMENT '需求数量 / Required quantity',
    delivery_time   DATETIME      NOT NULL                             COMMENT '要求到货时间 / Required delivery time',
    delivery_window_start DATETIME                                     COMMENT '交货窗口起始 / Delivery window start',
    delivery_window_end   DATETIME                                     COMMENT '交货窗口截止 / Delivery window end',
    dock_code       VARCHAR(32)                                        COMMENT '卸货码头编码 / Dock code',
    status          VARCHAR(16)   NOT NULL DEFAULT 'PENDING'           COMMENT '状态(PENDING/SHIPPED/DELIVERED/CANCELLED) / Status',
    actual_arrival  DATETIME                                           COMMENT '实际到货时间 / Actual arrival time',
    remark          VARCHAR(512)                                       COMMENT '备注 / Remark',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    INDEX idx_jit_order (purchase_order_id),
    INDEX idx_jit_status_time (status, delivery_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='JIT交货排程 / JIT delivery schedule';

-- -------------------------------------------------------------------------
-- 2. VMI库存监控 / VMI Inventory Monitoring
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS vmi_inventory (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    supplier_id     BIGINT        NOT NULL                             COMMENT '供应商ID / Supplier ID',
    plant_code      VARCHAR(16)   NOT NULL                             COMMENT '工厂编码 / Plant code',
    material_code   VARCHAR(64)   NOT NULL                             COMMENT '物料编码 / Material code',
    current_quantity DECIMAL(18,4) NOT NULL DEFAULT 0                  COMMENT '当前库存量 / Current quantity',
    min_threshold   DECIMAL(18,4)                                      COMMENT '最低库存阈值 / Min threshold',
    max_threshold   DECIMAL(18,4)                                      COMMENT '最高库存阈值 / Max threshold',
    last_replenish_time DATETIME                                       COMMENT '最近补货时间 / Last replenish time',
    warehouse_location VARCHAR(128)                                     COMMENT '库位 / Warehouse location',
    status          VARCHAR(16)   NOT NULL DEFAULT 'ACTIVE'            COMMENT '状态(ACTIVE/INACTIVE) / Status',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_vmi_supplier_plant_material (supplier_id, plant_code, material_code),
    INDEX idx_vmi_material (material_code),
    INDEX idx_vmi_plant (plant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='VMI库存监控 / VMI inventory monitoring';

-- -------------------------------------------------------------------------
-- 3. PPAP提交主表 / PPAP Submission Master
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ppap_submission (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    submission_no   VARCHAR(64)   NOT NULL                             COMMENT 'PPAP提交编号 / Submission number',
    supplier_id     BIGINT        NOT NULL                             COMMENT '供应商ID / Supplier ID',
    material_code   VARCHAR(64)   NOT NULL                             COMMENT '物料编码 / Material code',
    ppap_level      VARCHAR(8)    NOT NULL                             COMMENT 'PPAP等级(1/2/3/4/5) / PPAP level',
    submission_type VARCHAR(16)   NOT NULL                             COMMENT '提交类型(NEW/CHANGE/RESUBMIT) / Submission type',
    status          VARCHAR(16)   NOT NULL DEFAULT 'DRAFT'             COMMENT '状态(DRAFT/SUBMITTED/APPROVED/REJECTED) / Status',
    submitted_at    DATETIME                                            COMMENT '提交时间 / Submitted time',
    approved_at     DATETIME                                            COMMENT '批准时间 / Approved time',
    rejected_reason VARCHAR(1024)                                       COMMENT '驳回原因 / Rejection reason',
    created_by      BIGINT                                              COMMENT '创建人ID / Created by',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ppap_submission_no (submission_no),
    INDEX idx_ppap_supplier (supplier_id),
    INDEX idx_ppap_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='PPAP提交主表 / PPAP submission master';

-- -------------------------------------------------------------------------
-- 4. PPAP提交要素明细 / PPAP Submission Element Detail
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ppap_element (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    submission_id   BIGINT        NOT NULL                             COMMENT 'PPAP提交ID / PPAP submission ID',
    element_code    VARCHAR(32)   NOT NULL                             COMMENT '要素编码(1_design_record/2_engineering_change等) / Element code',
    element_name    VARCHAR(128)  NOT NULL                             COMMENT '要素名称 / Element name',
    submit_status   VARCHAR(16)   NOT NULL DEFAULT 'PENDING'           COMMENT '提交状态(PENDING/SUBMITTED/APPROVED/REJECTED) / Submit status',
    attachment_path VARCHAR(512)                                        COMMENT '附件路径 / Attachment path',
    reviewer_id     BIGINT                                              COMMENT '审核人ID / Reviewer ID',
    review_comment  VARCHAR(1024)                                       COMMENT '审核意见 / Review comment',
    reviewed_at     DATETIME                                            COMMENT '审核时间 / Reviewed time',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    PRIMARY KEY (id),
    INDEX idx_ppap_el_submission (submission_id),
    INDEX idx_ppap_el_code (element_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='PPAP提交要素明细 / PPAP submission element detail';

-- -------------------------------------------------------------------------
-- 5. 供应商工厂关联 / Supplier-Plant Relation
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS supplier_plant_rel (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    supplier_id     BIGINT        NOT NULL                             COMMENT '供应商ID / Supplier ID',
    plant_code      VARCHAR(16)   NOT NULL                             COMMENT '工厂编码 / Plant code',
    plant_name      VARCHAR(64)   NOT NULL                             COMMENT '工厂名称 / Plant name',
    distance_km     DECIMAL(10,2)                                      COMMENT '距离(公里) / Distance (km)',
    lead_time_days  INT                                                COMMENT '标准交期(天) / Standard lead time (days)',
    is_primary      TINYINT(1)    NOT NULL DEFAULT 0                   COMMENT '是否主供工厂 / Is primary plant',
    status          VARCHAR(16)   NOT NULL DEFAULT 'ACTIVE'            COMMENT '状态(ACTIVE/INACTIVE) / Status',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_supplier_plant (supplier_id, plant_code),
    INDEX idx_spr_plant (plant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='供应商工厂关联 / Supplier-plant relation';

-- -------------------------------------------------------------------------
-- 6. 来料批次追溯 / Incoming Lot Traceability
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS lot_trace (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    lot_no          VARCHAR(64)   NOT NULL                             COMMENT '批次号 / Lot number',
    material_code   VARCHAR(64)   NOT NULL                             COMMENT '物料编码 / Material code',
    supplier_id     BIGINT        NOT NULL                             COMMENT '供应商ID / Supplier ID',
    purchase_order_id BIGINT                                           COMMENT '采购订单ID / Purchase order ID',
    receiving_id    BIGINT                                              COMMENT '收货记录ID / Receiving record ID',
    quantity        DECIMAL(18,4) NOT NULL                             COMMENT '批次数量 / Lot quantity',
    production_date DATE                                                COMMENT '生产日期 / Production date',
    expiry_date     DATE                                                COMMENT '过期日期 / Expiry date',
    certificate_no  VARCHAR(64)                                        COMMENT '合格证编号 / Certificate number',
    status          VARCHAR(16)   NOT NULL DEFAULT 'RECEIVED'          COMMENT '状态(RECEIVED/QUARANTINED/RELEASED/REJECTED) / Status',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_lot_no (lot_no),
    INDEX idx_lot_material (material_code),
    INDEX idx_lot_supplier (supplier_id),
    INDEX idx_lot_order (purchase_order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='来料批次追溯 / Incoming lot traceability';

-- -------------------------------------------------------------------------
-- 7. 采购订单表扩展 / Purchase Order Extension
-- -------------------------------------------------------------------------
ALTER TABLE purchase_order
    ADD COLUMN IF NOT EXISTS delivery_window VARCHAR(64)  COMMENT '交货窗口(如 06-18~06-20) / Delivery window',
    ADD COLUMN IF NOT EXISTS plant_code       VARCHAR(16)  COMMENT '目标工厂编码 / Target plant code';

-- -------------------------------------------------------------------------
-- 8. 询价采购明细扩展 / Inquiry Purchase Detail Extension
-- -------------------------------------------------------------------------
ALTER TABLE inquiry_purchase
    ADD COLUMN IF NOT EXISTS target_plant VARCHAR(16) COMMENT '目标工厂 / Target plant';

-- -------------------------------------------------------------------------
-- 9. 商品表扩展（批次管理标记） / Goods Extension (lot management flag)
-- -------------------------------------------------------------------------
ALTER TABLE goods
    ADD COLUMN IF NOT EXISTS lot_managed TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否批次管理(1=是 0=否) / Lot managed flag';
