-- ============================================
-- Atlas V4: 采购订单
-- ============================================

CREATE DATABASE IF NOT EXISTS atlas_purchase DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE atlas_purchase;

CREATE TABLE IF NOT EXISTS purchase_order (
    id              BIGINT PRIMARY KEY COMMENT '订单ID(雪花算法)',
    order_no        VARCHAR(32) NOT NULL COMMENT '订单编号',
    contract_id     BIGINT COMMENT '关联合同ID',
    supplier_id     BIGINT NOT NULL COMMENT '供应商ID',
    dept_id         BIGINT NOT NULL COMMENT '需求部门ID',
    total_amount    DECIMAL(18,2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '0草稿 1待审批 2已审批 3执行中 4已完成 5已取消',
    request_id      VARCHAR(64) COMMENT '请求幂等ID',
    approved_by     BIGINT COMMENT '审批人',
    approved_at     DATETIME,
    created_by      BIGINT NOT NULL,
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    UNIQUE KEY uk_request_id (request_id),
    KEY idx_supplier_status (supplier_id, status),
    KEY idx_dept_status (dept_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购订单主表';

CREATE TABLE IF NOT EXISTS order_item (
    id              BIGINT PRIMARY KEY,
    order_id        BIGINT NOT NULL,
    sku_id          BIGINT NOT NULL COMMENT 'SKU ID',
    sku_name        VARCHAR(200) NOT NULL COMMENT '商品名称',
    quantity        DECIMAL(18,4) NOT NULL COMMENT '采购数量',
    unit_price      DECIMAL(18,2) NOT NULL COMMENT '单价',
    total_price     DECIMAL(18,2) NOT NULL COMMENT '小计',
    KEY idx_order_id (order_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='采购明细表';
