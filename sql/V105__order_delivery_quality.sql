-- ====================================================================
-- Atlas V105: 订单管理 + 交付物流 + 质量管理模块增强
-- 基于 09 报告 3.4/3.5/3.6 章节 P0+P1 优化
-- 日期：2026-06-18
-- ====================================================================

-- ============================================================
-- 3.4 订单管理 / Order Management
-- ============================================================

-- P0-3.4.2 订单变更管理（ECN）主表
CREATE TABLE IF NOT EXISTS order_change (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法） / Primary key (Snowflake)',
    change_no VARCHAR(64) NOT NULL COMMENT '变更单号 / Change order number',
    order_id BIGINT NOT NULL COMMENT '关联采购订单ID / Associated purchase order ID',
    change_type VARCHAR(32) NOT NULL COMMENT '变更类型：QTY_CHANGE数量变更/PRICE_CHANGE价格变更/DATE_CHANGE交期变更/ITEM_ADD增加明细/ITEM_REMOVE删除明细 / Change type',
    change_reason VARCHAR(500) NOT NULL COMMENT '变更原因 / Change reason',
    status VARCHAR(32) NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT草稿/PENDING_APPROVE待审批/APPROVED已审批/SUPPLIER_CONFIRMED供应商确认/EXECUTED已执行/REJECTED已驳回 / Status',
    approved_by BIGINT COMMENT '审批人ID / Approved by',
    approved_at DATETIME COMMENT '审批时间 / Approval time',
    supplier_confirmed_at DATETIME COMMENT '供应商确认时间 / Supplier confirmed time',
    created_by BIGINT NOT NULL COMMENT '创建人 / Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_change_no (change_no),
    INDEX idx_order_id (order_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单变更管理表 / Order change (ECN) table';

-- P0-3.4.2 订单变更明细表
CREATE TABLE IF NOT EXISTS order_change_detail (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法） / Primary key (Snowflake)',
    change_id BIGINT NOT NULL COMMENT '关联变更单ID / Associated change ID',
    field_name VARCHAR(64) NOT NULL COMMENT '变更字段名 / Changed field name',
    field_label VARCHAR(128) COMMENT '字段显示名 / Field display label',
    old_value TEXT COMMENT '变更前值 / Old value',
    new_value TEXT COMMENT '变更后值 / New value',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    INDEX idx_change_id (change_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单变更明细表 / Order change detail table';

-- P1-3.4.1 订单类型字段扩展
ALTER TABLE purchase_order
    ADD COLUMN IF NOT EXISTS order_type VARCHAR(32) DEFAULT 'STANDARD' COMMENT '订单类型：STANDARD标准/ BLANKET一揽子/ SCHEDULE_AGREEMENT计划协议 / Order type',
    ADD COLUMN IF NOT EXISTS blanket_effective_start DATE COMMENT '一揽子订单有效期起始 / Blanket order effective start',
    ADD COLUMN IF NOT EXISTS blanket_effective_end DATE COMMENT '一揽子订单有效期截止 / Blanket order effective end',
    ADD COLUMN IF NOT EXISTS blanket_total_qty DECIMAL(14,2) COMMENT '一揽子订单总量上限 / Blanket order total quantity cap',
    ADD COLUMN IF NOT EXISTS blanket_total_amount DECIMAL(14,2) COMMENT '一揽子订单金额上限 / Blanket order total amount cap',
    ADD COLUMN IF NOT EXISTS blanket_release_rule VARCHAR(64) COMMENT '分批规则：FIXED_SCHEDULE固定周期/ ON_DEMAND按需/MIN_MAX最小最大 / Release rule',
    ADD COLUMN IF NOT EXISTS blanket_consumed_qty DECIMAL(14,2) DEFAULT 0 COMMENT '已消耗数量 / Consumed quantity',
    ADD COLUMN IF NOT EXISTS blanket_consumed_amount DECIMAL(14,2) DEFAULT 0 COMMENT '已消耗金额 / Consumed amount',
    ADD INDEX IF NOT EXISTS idx_order_type (order_type);

-- P1-3.4.1 一揽子订单分批释放记录
CREATE TABLE IF NOT EXISTS blanket_order_release (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法） / Primary key (Snowflake)',
    blanket_order_id BIGINT NOT NULL COMMENT '关联一揽子订单ID / Associated blanket order ID',
    release_no VARCHAR(64) NOT NULL COMMENT '释放单号 / Release number',
    release_qty DECIMAL(14,2) NOT NULL COMMENT '本次释放数量 / Release quantity',
    release_amount DECIMAL(14,2) NOT NULL COMMENT '本次释放金额 / Release amount',
    release_date DATE NOT NULL COMMENT '释放日期 / Release date',
    expected_delivery_date DATE COMMENT '期望交货日 / Expected delivery date',
    status VARCHAR(32) DEFAULT 'RELEASED' COMMENT '状态：RELEASED已释放/ DELIVERED已交付/ CANCELLED已取消 / Status',
    created_by BIGINT COMMENT '创建人 / Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_release_no (release_no),
    INDEX idx_blanket_order (blanket_order_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='一揽子订单分批释放记录表 / Blanket order release table';

-- P0-3.4.3 MOQ/EOQ 物料字段扩展
ALTER TABLE material
    ADD COLUMN IF NOT EXISTS min_order_qty DECIMAL(14,2) COMMENT '最小起订量（MOQ） / Minimum order quantity',
    ADD COLUMN IF NOT EXISTS economic_order_qty DECIMAL(14,2) COMMENT '经济订货量（EOQ） / Economic order quantity',
    ADD COLUMN IF NOT EXISTS order_qty_multiple DECIMAL(14,2) COMMENT '订货量倍数（如必须按托盘数量订购） / Order quantity multiple';

-- P1-3.4.4 交期确认与预警
CREATE TABLE IF NOT EXISTS delivery_commitment (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法） / Primary key (Snowflake)',
    order_id BIGINT NOT NULL COMMENT '关联订单ID / Associated order ID',
    line_no INT NOT NULL COMMENT '订单行号 / Order line number',
    material_id BIGINT NOT NULL COMMENT '物料ID / Material ID',
    requested_date DATE NOT NULL COMMENT '需求交期 / Requested delivery date',
    committed_date DATE COMMENT '供应商承诺交期 / Supplier committed date',
    confirmed_date DATE COMMENT '实际确认交期 / Confirmed delivery date',
    actual_delivery_date DATE COMMENT '实际到货日 / Actual delivery date',
    deviation_days INT COMMENT '偏差天数（承诺-需求，正数为延迟） / Deviation days',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    alerted TINYINT DEFAULT 0 COMMENT '是否已预警：0未预警 1已预警 / Whether alerted',
    alert_sent_at DATETIME COMMENT '预警发送时间 / Alert sent time',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    INDEX idx_order_line (order_id, line_no),
    INDEX idx_supplier (supplier_id),
    INDEX idx_delivery_date (committed_date),
    INDEX idx_deviation (deviation_days)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交期承诺与预警表 / Delivery commitment & alert table';

-- ============================================================
-- 3.5 交付物流 / Delivery & Logistics
-- ============================================================

-- P0-3.5.1 ASN 预先发货通知主表
CREATE TABLE IF NOT EXISTS asn_record (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法） / Primary key (Snowflake)',
    asn_no VARCHAR(64) NOT NULL COMMENT 'ASN单号 / ASN number',
    order_id BIGINT NOT NULL COMMENT '关联采购订单ID / Associated purchase order ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    expected_arrival_date DATE COMMENT '预计到货日 / Expected arrival date',
    ship_date DATE COMMENT '实际发货日 / Actual ship date',
    carrier VARCHAR(100) COMMENT '承运商 / Carrier name',
    tracking_no VARCHAR(100) COMMENT '物流单号 / Tracking number',
    vehicle_no VARCHAR(50) COMMENT '车牌号 / Vehicle plate number',
    driver_name VARCHAR(50) COMMENT '司机姓名 / Driver name',
    driver_phone VARCHAR(30) COMMENT '司机电话 / Driver phone',
    status VARCHAR(32) DEFAULT 'CREATED' COMMENT '状态：CREATED已创建/ IN_TRANSIT在途/ ARRIVED已到货/ RECEIVED已收货/ CANCELLED已取消 / Status',
    remark VARCHAR(500) COMMENT '备注 / Remark',
    created_by BIGINT COMMENT '创建人 / Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_asn_no (asn_no),
    INDEX idx_order_id (order_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_arrival (expected_arrival_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ASN预先发货通知表 / ASN (Advanced Shipping Notice) record table';

-- P0-3.5.1 ASN 明细表
CREATE TABLE IF NOT EXISTS asn_item (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法） / Primary key (Snowflake)',
    asn_id BIGINT NOT NULL COMMENT '关联ASN ID / Associated ASN ID',
    material_id BIGINT NOT NULL COMMENT '物料ID / Material ID',
    material_name VARCHAR(200) COMMENT '物料名称 / Material name',
    quantity DECIMAL(14,2) NOT NULL COMMENT '发货数量 / Shipped quantity',
    unit VARCHAR(20) COMMENT '单位 / Unit',
    batch_no VARCHAR(64) COMMENT '生产批次号 / Production batch number',
    packaging_type VARCHAR(50) COMMENT '包装方式 / Packaging type',
    package_count INT COMMENT '件数 / Package count',
    gross_weight DECIMAL(14,2) COMMENT '毛重(kg) / Gross weight',
    net_weight DECIMAL(14,2) COMMENT '净重(kg) / Net weight',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    INDEX idx_asn_id (asn_id),
    INDEX idx_material (material_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='ASN明细表 / ASN item table';

-- P0-3.5.5 收货质检联动表
CREATE TABLE IF NOT EXISTS receiving_record (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法） / Primary key (Snowflake)',
    receive_no VARCHAR(64) NOT NULL COMMENT '收货单号 / Receiving number',
    asn_id BIGINT COMMENT '关联ASN ID / Associated ASN ID',
    order_id BIGINT NOT NULL COMMENT '关联采购订单ID / Associated purchase order ID',
    supplier_id BIGINT NOT NULL COMMENT '供应商ID / Supplier ID',
    warehouse_id BIGINT COMMENT '收货仓库ID / Receiving warehouse ID',
    receive_date DATETIME NOT NULL COMMENT '收货日期 / Receiving date',
    receiver_id BIGINT COMMENT '收货人ID / Receiver ID',
    inspection_triggered TINYINT DEFAULT 0 COMMENT '是否已触发质检：0未触发 1已触发 / Whether inspection triggered',
    inspection_id BIGINT COMMENT '关联检验单ID（IQC） / Associated inspection ID',
    inspection_result VARCHAR(32) COMMENT '质检结果：PASS合格/ REJECT退货 / Inspection result',
    receipt_id BIGINT COMMENT '关联收货确认单ID / Associated receipt confirmation ID',
    status VARCHAR(32) DEFAULT 'RECEIVED' COMMENT '状态：RECEIVED已收货/ INSPECTING质检中/ ACCEPTED合格入库/ REJECTED退货 / Status',
    remark VARCHAR(500) COMMENT '备注 / Remark',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_receive_no (receive_no),
    INDEX idx_order_id (order_id),
    INDEX idx_asn_id (asn_id),
    INDEX idx_status (status),
    INDEX idx_receive_date (receive_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货质检联动记录表 / Receiving & quality inspection linkage table';

-- ============================================================
-- 3.6 质量管理 / Quality Management
-- ============================================================

-- P0-3.6.1 检验标准与抽样方案
CREATE TABLE IF NOT EXISTS inspection_standard (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法） / Primary key (Snowflake)',
    standard_no VARCHAR(64) NOT NULL COMMENT '标准编号 / Standard number',
    material_id BIGINT NOT NULL COMMENT '物料ID / Material ID',
    material_name VARCHAR(200) COMMENT '物料名称 / Material name',
    inspect_type VARCHAR(16) NOT NULL COMMENT '检验类型：IQC来料检验/IPQC过程检验/OQC出货检验 / Inspection type',
    aql_level VARCHAR(16) COMMENT 'AQL水平：0.010/0.015/0.025/0.040/0.065/0.10/0.15/0.25/0.40/0.65/1.0/1.5/2.5/4.0/6.5 / AQL level',
    sample_size_type VARCHAR(16) COMMENT '抽样方案：GB2828/S-1/S-2/S-3/S-4/100_PERCENT全检 / Sample size type',
    inspection_level VARCHAR(8) DEFAULT 'II' COMMENT '检验水平：I/II/III/S-1/S-2/S-3/S-4 / Inspection level',
    sample_size INT COMMENT '抽样数量 / Sample size',
    accept_level TINYINT COMMENT '合格判定数（Ac） / Acceptance number',
    reject_level TINYINT COMMENT '不合格判定数（Re） / Rejection number',
    inspection_items TEXT COMMENT '检验项目清单（JSON：每个项目含名称/方法/工具/规格上下限/单位） / Inspection item checklist (JSON)',
    is_active TINYINT DEFAULT 1 COMMENT '是否启用：0停用 1启用 / Whether active',
    created_by BIGINT COMMENT '创建人 / Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_standard_no (standard_no),
    INDEX idx_material_type (material_id, inspect_type),
    INDEX idx_is_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='检验标准与抽样方案表 / Inspection standard & sampling plan table';

-- P0-3.6.3 不合格品处理（NCR）
CREATE TABLE IF NOT EXISTS ncr_record (
    id BIGINT PRIMARY KEY COMMENT '主键ID（雪花算法） / Primary key (Snowflake)',
    ncr_no VARCHAR(64) NOT NULL COMMENT 'NCR单号 / NCR number',
    inspect_id BIGINT COMMENT '关联检验单ID / Associated inspection ID',
    material_id BIGINT NOT NULL COMMENT '物料ID / Material ID',
    material_name VARCHAR(200) COMMENT '物料名称 / Material name',
    batch_no VARCHAR(64) COMMENT '生产批次号 / Production batch number',
    supplier_id BIGINT COMMENT '供应商ID / Supplier ID',
    defect_type VARCHAR(64) NOT NULL COMMENT '缺陷类型：DIMENSION尺寸/ APPEARANCE外观/ FUNCTION功能/ MATERIAL材质/ PACKAGING包装/ OTHER其他 / Defect type',
    defect_description VARCHAR(500) COMMENT '缺陷描述 / Defect description',
    defect_qty DECIMAL(14,2) NOT NULL COMMENT '不合格数量 / Defect quantity',
    defect_severity VARCHAR(16) DEFAULT 'MINOR' COMMENT '严重程度：CRITICAL致命/ MAJOR重大/ MINOR轻微 / Severity',
    disposition VARCHAR(32) COMMENT '处置方式：ACCEPT让步接收/ CONCESSION特采/ SORT挑选/ RETURN退货/ SCRAP报废 / Disposition',
    disposition_by BIGINT COMMENT '处置人ID / Disposition by',
    disposition_at DATETIME COMMENT '处置时间 / Disposition time',
    disposition_reason VARCHAR(500) COMMENT '处置理由 / Disposition reason',
    corrective_action VARCHAR(500) COMMENT '纠正措施 / Corrective action',
    closed TINYINT DEFAULT 0 COMMENT '是否闭环：0未闭环 1已闭环 / Whether closed',
    closed_at DATETIME COMMENT '闭环时间 / Closed time',
    created_by BIGINT COMMENT '创建人 / Created by',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间 / Created time',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间 / Updated time',
    UNIQUE KEY uk_ncr_no (ncr_no),
    INDEX idx_inspect_id (inspect_id),
    INDEX idx_material (material_id),
    INDEX idx_supplier (supplier_id),
    INDEX idx_disposition (disposition),
    INDEX idx_closed (closed)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='不合格品处理记录表（NCR） / Non-Conformance Report table';
