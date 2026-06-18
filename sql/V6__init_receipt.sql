-- ============================================
-- Atlas V6: 收货单
-- ============================================

CREATE DATABASE IF NOT EXISTS atlas_receipt DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE atlas_receipt;

CREATE TABLE IF NOT EXISTS receipt (
    id              BIGINT PRIMARY KEY,
    receipt_no      VARCHAR(32) NOT NULL COMMENT '收货单编号',
    order_id        BIGINT NOT NULL COMMENT '关联采购订单',
    warehouse_id    BIGINT NOT NULL COMMENT '收货仓库',
    supplier_id     BIGINT NOT NULL COMMENT '供应商ID',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '0待收货 1部分收货 2全部收货 3质检中 4已入库',
    inspector_id    BIGINT COMMENT '质检人',
    inspected_at    DATETIME,
    created_by      BIGINT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_receipt_no (receipt_no),
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货单主表';

CREATE TABLE IF NOT EXISTS receipt_item (
    id              BIGINT PRIMARY KEY,
    receipt_id      BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL,
    order_qty       DECIMAL(18,4) NOT NULL COMMENT '订单数量',
    received_qty    DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '实收数量',
    qualified_qty   DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '合格数量',
    reject_reason   VARCHAR(200) COMMENT '不合格原因',
    KEY idx_receipt_id (receipt_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货明细表';
