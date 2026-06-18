-- ============================================
-- Atlas V5: 库存管理
-- ============================================

CREATE DATABASE IF NOT EXISTS atlas_inventory DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE atlas_inventory;

CREATE TABLE IF NOT EXISTS inventory (
    id              BIGINT PRIMARY KEY,
    sku_id          BIGINT NOT NULL COMMENT 'SKU ID',
    warehouse_id    BIGINT NOT NULL COMMENT '仓库ID',
    quantity        DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '当前库存',
    locked_qty      DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '已锁定(采购在途)',
    safety_stock    DECIMAL(18,4) NOT NULL DEFAULT 0 COMMENT '安全库存阈值',
    version         INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_sku_warehouse (sku_id, warehouse_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存表';

CREATE TABLE IF NOT EXISTS inventory_log (
    id              BIGINT PRIMARY KEY,
    sku_id          BIGINT NOT NULL,
    warehouse_id    BIGINT NOT NULL,
    change_type     TINYINT NOT NULL COMMENT '1采购入库 2销售出库 3退货入库 4盘点调整',
    change_qty      DECIMAL(18,4) NOT NULL COMMENT '变动数量(正增负减)',
    before_qty      DECIMAL(18,4) NOT NULL COMMENT '变动前库存',
    after_qty       DECIMAL(18,4) NOT NULL COMMENT '变动后库存',
    order_no        VARCHAR(32) COMMENT '关联订单号',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_sku_warehouse_time (sku_id, warehouse_id, created_at),
    KEY idx_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='库存变动流水表';

-- Seata AT 模式 undo_log 表
CREATE TABLE IF NOT EXISTS undo_log (
    id              BIGINT NOT NULL AUTO_INCREMENT,
    branch_id       BIGINT NOT NULL,
    xid             VARCHAR(100) NOT NULL,
    context         VARCHAR(128) NOT NULL,
    rollback_info   LONGBLOB NOT NULL,
    log_status      INT NOT NULL,
    log_created     DATETIME NOT NULL,
    log_modified    DATETIME NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Seata undo_log表';
