-- ===================================================
-- V93: Seata AT 分布式事务 — undo_log 表
-- 每个业务数据库均需执行此 DDL（atlas_purchase / atlas_receipt / atlas_supplier 等）
-- ===================================================

CREATE TABLE IF NOT EXISTS undo_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    branch_id BIGINT NOT NULL,
    xid VARCHAR(128) NOT NULL,
    context VARCHAR(128) NOT NULL,
    rollback_info LONGBLOB NOT NULL,
    log_status INT NOT NULL,
    log_created DATETIME NOT NULL,
    log_modified DATETIME NOT NULL,
    UNIQUE KEY ux_undo_log (xid, branch_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
