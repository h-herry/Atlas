-- ============================================
-- Atlas V103: 制造业场景专项 — 全部建表 / Manufacturing-Specific Tables
-- 涵盖：JIT 交付排程 / VMI 库存 / PPAP 提交 / 供应商工厂关联 / 批次追溯 /
-- Covers: JIT Delivery Schedule / VMI Inventory / PPAP Submission / Supplier-Plant Relation / Lot Trace
-- ============================================

-- ============================================
-- 1. JIT 交货排程表 / JIT Delivery Schedule
-- ============================================
CREATE TABLE IF NOT EXISTS jit_delivery_schedule (
    id                  BIGINT PRIMARY KEY COMMENT '主键 / Primary key',
    order_id            BIGINT NOT NULL COMMENT '关联订单ID / Related order ID',
    delivery_date       DATE NOT NULL COMMENT '交货日期 / Delivery date',
    window_start        TIME NOT NULL COMMENT '交货窗口开始 / Delivery window start',
    window_end          TIME NOT NULL COMMENT '交货窗口结束 / Delivery window end',
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING-待确认 CONFIRMED-已确认 MISSED-超时未确认 / Status: PENDING/Confirmed/MISSED',
    confirm_time        DATETIME COMMENT '供应商确认时间 / Supplier confirmation time',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at          DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_order_id (order_id),
    INDEX idx_delivery_date (delivery_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='JIT交货排程表 / JIT delivery schedule';

-- ============================================
-- 2. VMI 库存监控表 / VMI Inventory Monitoring
-- ============================================
CREATE TABLE IF NOT EXISTS vmi_inventory (
    id                  BIGINT PRIMARY KEY COMMENT '主键 / Primary key',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    material_id         BIGINT NOT NULL COMMENT '物料ID / Material ID',
    warehouse_code      VARCHAR(50) NOT NULL COMMENT '仓库编码 / Warehouse code',
    current_stock       DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '当前库存 / Current stock',
    min_safety_stock    DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '安全库存下限 / Min safety stock',
    max_stock           DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '最大库存上限 / Max stock',
    replenish_point     DECIMAL(18,4) COMMENT '补货点 / Replenish point',
    in_transit_qty      DECIMAL(18,4) DEFAULT 0 COMMENT '在途量 / In-transit quantity',
    allocated_qty       DECIMAL(18,4) DEFAULT 0 COMMENT '已分配量 / Allocated quantity',
    last_update_time    DATETIME COMMENT '最后更新时间 / Last update time',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    UNIQUE KEY uk_supplier_material_warehouse (supplier_id, material_id, warehouse_code),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_material_id (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='VMI库存监控表 / VMI inventory monitoring';

-- ============================================
-- 3. PPAP 提交主表 / PPAP Submission
-- ============================================
CREATE TABLE IF NOT EXISTS ppap_submission (
    id                  BIGINT PRIMARY KEY COMMENT '主键 / Primary key',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    material_id         BIGINT NOT NULL COMMENT '物料ID / Material ID',
    ppap_level          INT NOT NULL DEFAULT 3 COMMENT 'PPAP等级(1~5) / PPAP level (1~5)',
    submission_date     DATE COMMENT '提交日期 / Submission date',
    status              VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/SUBMITTED/UNDER_REVIEW/INTERIM_APPROVED/FULLY_APPROVED/REJECTED',
    approval_date       DATE COMMENT '批准日期 / Approval date',
    approved_by         BIGINT COMMENT '批准人 / Approved by',
    project_code        VARCHAR(50) COMMENT '关联项目编码 / Related project code',
    sop_date            DATE COMMENT 'SOP节点日期 / SOP milestone date',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at          DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_material_id (material_id),
    INDEX idx_status (status),
    INDEX idx_project_code (project_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PPAP提交主表 / PPAP submission master';

-- ============================================
-- 4. PPAP 18要素明细表 / PPAP Element Detail
-- ============================================
CREATE TABLE IF NOT EXISTS ppap_element (
    id                  BIGINT PRIMARY KEY COMMENT '主键 / Primary key',
    submission_id       BIGINT NOT NULL COMMENT '关联PPAP提交ID / Related PPAP submission ID',
    element_code        VARCHAR(20) NOT NULL COMMENT '要素编码(如 DFMEA/PFMEA/CONTROL_PLAN) / Element code (e.g. DFMEA/PFMEA/CONTROL_PLAN)',
    element_name        VARCHAR(100) NOT NULL COMMENT '要素名称 / Element name',
    element_seq         INT COMMENT '要素序号(1~18) / Element sequence (1~18)',
    is_required         TINYINT(1) DEFAULT 1 COMMENT '当前PPAP等级是否要求本要素 / Whether required for current PPAP level',
    submitted           TINYINT(1) DEFAULT 0 COMMENT '是否已提交 / Whether submitted',
    approved            TINYINT(1) DEFAULT 0 COMMENT '是否已批准 / Whether approved',
    comment             VARCHAR(500) COMMENT '审核意见 / Review comment',
    file_path           VARCHAR(500) COMMENT '提交文件路径 / Submission file path',
    reviewed_by         BIGINT COMMENT '审核人 / Reviewed by',
    reviewed_at         DATETIME COMMENT '审核时间 / Review time',
    INDEX idx_submission_id (submission_id),
    UNIQUE KEY uk_submission_element (submission_id, element_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PPAP要素明细表 / PPAP element detail';

-- ============================================
-- 5. 供应商工厂关联表 / Supplier-Plant Relation
-- ============================================
CREATE TABLE IF NOT EXISTS supplier_plant_rel (
    id                  BIGINT PRIMARY KEY COMMENT '主键 / Primary key',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    plant_code          VARCHAR(20) NOT NULL COMMENT '工厂编码 / Plant code',
    priority            INT DEFAULT 0 COMMENT '优先级(0-最高) / Priority (0-highest)',
    lead_time_days      INT DEFAULT 0 COMMENT '供货提前期(天) / Lead time (days)',
    capacity_per_month  DECIMAL(18,4) COMMENT '月度产能上限 / Monthly capacity cap',
    is_active           TINYINT(1) DEFAULT 1 COMMENT '是否启用 / Whether active',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    UNIQUE KEY uk_supplier_plant (supplier_id, plant_code),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_plant_code (plant_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商-工厂关联表 / Supplier-Plant relation';

-- ============================================
-- 6. 来料批次追溯表 / Incoming Lot Traceability
-- ============================================
CREATE TABLE IF NOT EXISTS lot_trace (
    id                  BIGINT PRIMARY KEY COMMENT '主键 / Primary key',
    lot_no              VARCHAR(50) NOT NULL COMMENT '批次号 / Lot number',
    material_id         BIGINT NOT NULL COMMENT '物料ID / Material ID',
    supplier_id         BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    order_id            BIGINT COMMENT '关联采购订单ID / Related purchase order ID',
    receive_date        DATE COMMENT '收货日期 / Receive date',
    quantity            DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '批次数量 / Lot quantity',
    status              VARCHAR(20) NOT NULL DEFAULT 'RECEIVED' COMMENT '状态: RECEIVED/INSPECTING/QUALIFIED/REJECTED/CONSUMED',
    inspection_result   VARCHAR(20) COMMENT '检验结果: PASS/FAIL / Inspection result',
    inspection_date     DATETIME COMMENT '检验日期 / Inspection date',
    rejection_reason    VARCHAR(500) COMMENT '不合格原因 / Rejection reason',
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at          DATETIME ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_lot_no (lot_no),
    INDEX idx_material_id (material_id),
    INDEX idx_supplier_id (supplier_id),
    INDEX idx_order_id (order_id),
    INDEX idx_receive_date (receive_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='来料批次追溯表 / Incoming lot traceability';

-- ============================================
-- 7. 在 purchase_order 表增加制造业字段 / Add manufacturing fields to purchase_order
-- (通过 ALTER TABLE 方式增加，避免重复建表) / (Add via ALTER TABLE to avoid re-creating)
-- ============================================
-- 增加 delivery_window 字段（JIT交货窗口） / Add delivery_window field (JIT delivery window)
ALTER TABLE purchase_order
    ADD COLUMN IF NOT EXISTS delivery_window VARCHAR(50) COMMENT 'JIT交货窗口(如 2026-06-19 08:00-14:00) / JIT delivery window (e.g. 2026-06-19 08:00-14:00)';

-- 增加 plant_code 字段（所属工厂） / Add plant_code field (assigned plant)
ALTER TABLE purchase_order
    ADD COLUMN IF NOT EXISTS plant_code VARCHAR(20) COMMENT '所属工厂编码 / Assigned plant code';

-- 在询价单增加 target_plant 字段 / Add target_plant field to inquiry sheets
ALTER TABLE inquiry_purchase
    ADD COLUMN IF NOT EXISTS target_plant VARCHAR(20) COMMENT '目标工厂编码 / Target plant code';

-- ============================================
-- 8. 在物料表增加 lot_managed 字段 / Add lot_managed to material table
-- ============================================
ALTER TABLE goods
    ADD COLUMN IF NOT EXISTS lot_managed TINYINT(1) DEFAULT 0 COMMENT '是否启用批次管理: 0-否 1-是 / Whether lot management enabled: 0-no 1-yes';
