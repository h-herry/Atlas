-- ============================================
-- Atlas V2: 供应商管理
-- ============================================

CREATE DATABASE IF NOT EXISTS atlas_supplier DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE atlas_supplier;

CREATE TABLE IF NOT EXISTS supplier (
    id              BIGINT PRIMARY KEY COMMENT '供应商ID',
    supplier_code   VARCHAR(32) NOT NULL COMMENT '供应商编码',
    supplier_name   VARCHAR(100) NOT NULL COMMENT '供应商名称',
    supplier_type   TINYINT NOT NULL COMMENT '1生产商 2代理商 3服务商',
    contact_name    VARCHAR(50) COMMENT '联系人',
    contact_phone   VARCHAR(20) COMMENT '联系电话',
    contact_email   VARCHAR(100) COMMENT '联系邮箱',
    address         VARCHAR(200) COMMENT '地址',
    grade           TINYINT NOT NULL DEFAULT 1 COMMENT '评级:1-A 2-B 3-C 4-D',
    status          TINYINT NOT NULL DEFAULT 0 COMMENT '0待审核 1已准入 2已冻结 3已拉黑',
    created_by      BIGINT NOT NULL COMMENT '创建人',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_supplier_code (supplier_code),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商主表';

CREATE TABLE IF NOT EXISTS supplier_qualification (
    id              BIGINT PRIMARY KEY,
    supplier_id     BIGINT NOT NULL,
    qual_type       TINYINT NOT NULL COMMENT '1营业执照 2经营许可证 3ISO认证',
    file_url        VARCHAR(500) COMMENT '资质文件URL',
    expire_date     DATE COMMENT '到期日期',
    status          TINYINT NOT NULL DEFAULT 1 COMMENT '0过期 1有效',
    KEY idx_supplier_id (supplier_id),
    KEY idx_expire_date (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='供应商资质表';
