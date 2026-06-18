-- =============================================================================
-- V105__order_delivery_quality.sql
-- 订单变更 / 交期确认 / ASN / 收货 / 质量检验 / Order Change / Delivery / ASN / Receiving / Quality
-- 日期: 2026-06-18 / Date: 2026-06-18
-- =============================================================================

-- -------------------------------------------------------------------------
-- 1. 订单变更主表 / Order Change Master
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_change (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    change_no       VARCHAR(64)   NOT NULL                             COMMENT '变更单号 / Change number',
    purchase_order_id BIGINT      NOT NULL                             COMMENT '采购订单ID / Purchase order ID',
    change_type     VARCHAR(16)   NOT NULL                             COMMENT '变更类型(QUANTITY/PRICE/DELIVERY/SPEC/CANCEL) / Change type',
    change_reason   VARCHAR(512)  NOT NULL                             COMMENT '变更原因 / Change reason',
    status          VARCHAR(16)   NOT NULL DEFAULT 'DRAFT'             COMMENT '状态(DRAFT/SUBMITTED/APPROVED/REJECTED) / Status',
    applicant_id    BIGINT        NOT NULL                             COMMENT '申请人ID / Applicant ID',
    approved_by     BIGINT                                              COMMENT '审批人ID / Approved by',
    approved_at     DATETIME                                            COMMENT '审批时间 / Approved time',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_order_change_no (change_no),
    INDEX idx_oc_order (purchase_order_id),
    INDEX idx_oc_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='订单变更主表 / Order change master';

-- -------------------------------------------------------------------------
-- 2. 订单变更明细 / Order Change Detail
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS order_change_detail (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    change_id       BIGINT        NOT NULL                             COMMENT '变更单ID / Change ID',
    material_code   VARCHAR(64)   NOT NULL                             COMMENT '物料编码 / Material code',
    field_name      VARCHAR(64)   NOT NULL                             COMMENT '变更字段 / Changed field',
    old_value       VARCHAR(512)                                       COMMENT '原值 / Old value',
    new_value       VARCHAR(512)  NOT NULL                             COMMENT '新值 / New value',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    PRIMARY KEY (id),
    INDEX idx_ocd_change (change_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='订单变更明细 / Order change detail';

-- -------------------------------------------------------------------------
-- 3. 一揽子订单释放 / Blanket Order Release
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS blanket_order_release (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    release_no      VARCHAR(64)   NOT NULL                             COMMENT '释放单号 / Release number',
    blanket_order_id BIGINT       NOT NULL                             COMMENT '一揽子订单ID / Blanket order ID',
    material_code   VARCHAR(64)   NOT NULL                             COMMENT '物料编码 / Material code',
    release_quantity DECIMAL(18,4) NOT NULL                            COMMENT '释放数量 / Release quantity',
    required_date   DATE          NOT NULL                             COMMENT '需求日期 / Required date',
    plant_code      VARCHAR(16)                                        COMMENT '目标工厂 / Plant code',
    status          VARCHAR(16)   NOT NULL DEFAULT 'RELEASED'          COMMENT '状态(RELEASED/RECEIVED/CLOSED) / Status',
    released_by     BIGINT                                              COMMENT '释放人ID / Released by',
    released_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '释放时间 / Released time',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_release_no (release_no),
    INDEX idx_bor_order (blanket_order_id),
    INDEX idx_bor_date (required_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='一揽子订单释放 / Blanket order release';

-- -------------------------------------------------------------------------
-- 4. 交期确认 / Delivery Commitment
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS delivery_commitment (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    purchase_order_id BIGINT      NOT NULL                             COMMENT '采购订单ID / Purchase order ID',
    line_no         INT           NOT NULL                             COMMENT '订单行号 / Line number',
    material_code   VARCHAR(64)   NOT NULL                             COMMENT '物料编码 / Material code',
    committed_qty   DECIMAL(18,4) NOT NULL                             COMMENT '承诺数量 / Committed quantity',
    committed_date  DATE          NOT NULL                             COMMENT '承诺交期 / Committed date',
    confirmed_by    VARCHAR(64)                                        COMMENT '确认人 / Confirmed by',
    status          VARCHAR(16)   NOT NULL DEFAULT 'PENDING'           COMMENT '状态(PENDING/CONFIRMED/OVERDUE) / Status',
    remark          VARCHAR(512)                                       COMMENT '备注 / Remark',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    INDEX idx_dc_order (purchase_order_id),
    INDEX idx_dc_date (committed_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='交期确认 / Delivery commitment';

-- -------------------------------------------------------------------------
-- 5. ASN主表 / Advanced Shipping Notice Master
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS asn_record (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    asn_no          VARCHAR(64)   NOT NULL                             COMMENT 'ASN单号 / ASN number',
    purchase_order_id BIGINT      NOT NULL                             COMMENT '采购订单ID / Purchase order ID',
    supplier_id     BIGINT        NOT NULL                             COMMENT '供应商ID / Supplier ID',
    carrier         VARCHAR(64)                                        COMMENT '承运商 / Carrier',
    tracking_no     VARCHAR(64)                                        COMMENT '运单号 / Tracking number',
    estimated_arrival DATETIME                                         COMMENT '预计到达时间 / Estimated arrival',
    status          VARCHAR(16)   NOT NULL DEFAULT 'IN_TRANSIT'        COMMENT '状态(IN_TRANSIT/RECEIVED/CANCELLED) / Status',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_asn_no (asn_no),
    INDEX idx_asn_order (purchase_order_id),
    INDEX idx_asn_supplier (supplier_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='ASN主表 / ASN master';

-- -------------------------------------------------------------------------
-- 6. ASN明细 / ASN Item
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS asn_item (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    asn_id          BIGINT        NOT NULL                             COMMENT 'ASN主表ID / ASN ID',
    material_code   VARCHAR(64)   NOT NULL                             COMMENT '物料编码 / Material code',
    shipped_qty     DECIMAL(18,4) NOT NULL                             COMMENT '发货数量 / Shipped quantity',
    lot_no          VARCHAR(64)                                        COMMENT '批次号 / Lot number',
    package_count   INT          DEFAULT 1                             COMMENT '包装件数 / Package count',
    gross_weight    DECIMAL(18,4)                                      COMMENT '毛重(kg) / Gross weight',
    net_weight      DECIMAL(18,4)                                      COMMENT '净重(kg) / Net weight',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    PRIMARY KEY (id),
    INDEX idx_asn_item_asn (asn_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='ASN明细 / ASN item';

-- -------------------------------------------------------------------------
-- 7. 收货记录 / Receiving Record
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS receiving_record (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    receiving_no    VARCHAR(64)   NOT NULL                             COMMENT '收货单号 / Receiving number',
    asn_id          BIGINT                                              COMMENT '关联ASN ID / Related ASN ID',
    purchase_order_id BIGINT      NOT NULL                             COMMENT '采购订单ID / Purchase order ID',
    supplier_id     BIGINT        NOT NULL                             COMMENT '供应商ID / Supplier ID',
    received_at     DATETIME      NOT NULL                             COMMENT '收货时间 / Received time',
    received_by     BIGINT                                              COMMENT '收货人ID / Received by',
    warehouse_code  VARCHAR(32)                                        COMMENT '仓库编码 / Warehouse code',
    status          VARCHAR(16)   NOT NULL DEFAULT 'RECEIVED'          COMMENT '状态(RECEIVED/INSPECTING/ACCEPTED/REJECTED) / Status',
    quality_status  VARCHAR(16)   DEFAULT 'PENDING'                    COMMENT '质检状态(PENDING/PASSED/FAILED) / Quality status',
    remark          VARCHAR(512)                                       COMMENT '备注 / Remark',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_receiving_no (receiving_no),
    INDEX idx_rec_order (purchase_order_id),
    INDEX idx_rec_supplier (supplier_id),
    INDEX idx_rec_asn (asn_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='收货记录 / Receiving record';

-- -------------------------------------------------------------------------
-- 8. 检验标准 / Inspection Standard
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS inspection_standard (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    standard_code   VARCHAR(64)   NOT NULL                             COMMENT '标准编码 / Standard code',
    material_code   VARCHAR(64)   NOT NULL                             COMMENT '物料编码 / Material code',
    inspection_type VARCHAR(16)   NOT NULL                             COMMENT '检验类型(FULL/SAMPLING/VISUAL) / Inspection type',
    sampling_ratio  DECIMAL(5,4)                                       COMMENT '抽样比例 / Sampling ratio',
    aql_level       VARCHAR(16)                                        COMMENT 'AQL等级 / AQL level',
    critical_defect DECIMAL(18,4)                                      COMMENT '致命缺陷指标 / Critical defect threshold',
    major_defect    DECIMAL(18,4)                                      COMMENT '主要缺陷指标 / Major defect threshold',
    minor_defect    DECIMAL(18,4)                                      COMMENT '次要缺陷指标 / Minor defect threshold',
    status          VARCHAR(16)   NOT NULL DEFAULT 'ACTIVE'            COMMENT '状态(ACTIVE/INACTIVE) / Status',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ins_standard (standard_code),
    INDEX idx_ins_material (material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='检验标准 / Inspection standard';

-- -------------------------------------------------------------------------
-- 9. 不合格品处理 / Non-Conformance Record (NCR)
-- -------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS ncr_record (
    id              BIGINT        NOT NULL AUTO_INCREMENT              COMMENT '主键 / Primary key',
    ncr_no          VARCHAR(64)   NOT NULL                             COMMENT 'NCR编号 / NCR number',
    receiving_id    BIGINT                                              COMMENT '收货记录ID / Receiving record ID',
    material_code   VARCHAR(64)   NOT NULL                             COMMENT '物料编码 / Material code',
    lot_no          VARCHAR(64)                                        COMMENT '批次号 / Lot number',
    defect_qty      DECIMAL(18,4) NOT NULL                             COMMENT '不良数量 / Defect quantity',
    defect_type     VARCHAR(16)   NOT NULL                             COMMENT '缺陷等级(CRITICAL/MAJOR/MINOR) / Defect type',
    defect_desc     VARCHAR(512)  NOT NULL                             COMMENT '缺陷描述 / Defect description',
    disposition     VARCHAR(16)   NOT NULL DEFAULT 'PENDING'           COMMENT '处理方式(RETURN/REWORK/ACCEPT_AS_IS/SCRAP) / Disposition',
    supplier_notified TINYINT(1)  DEFAULT 0                            COMMENT '是否已通知供应商 / Supplier notified',
    closed_at       DATETIME                                            COMMENT '关闭时间 / Closed time',
    created_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP   COMMENT '创建时间 / Created time',
    updated_at      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    PRIMARY KEY (id),
    UNIQUE KEY uk_ncr_no (ncr_no),
    INDEX idx_ncr_receiving (receiving_id),
    INDEX idx_ncr_material (material_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='不合格品处理 / NCR record';

-- -------------------------------------------------------------------------
-- 10. 采购订单扩展（订单类型 + 一揽子订单字段） / Purchase Order Extension
-- -------------------------------------------------------------------------
ALTER TABLE purchase_order
    ADD COLUMN IF NOT EXISTS order_type         VARCHAR(16)  COMMENT '订单类型(STANDARD/BLANKET/SCHEDULE) / Order type',
    ADD COLUMN IF NOT EXISTS blanket_start_date DATE         COMMENT '一揽子订单起始日 / Blanket start date',
    ADD COLUMN IF NOT EXISTS blanket_end_date   DATE         COMMENT '一揽子订单截止日 / Blanket end date',
    ADD COLUMN IF NOT EXISTS blanket_total_qty  DECIMAL(18,4) COMMENT '一揽子订单总量 / Blanket total quantity';
