-- ============================================
-- Atlas V11: 竞价大厅
-- 数据库: atlas_purchase
-- ============================================

USE atlas_purchase;

-- 竞价大厅主表
CREATE TABLE IF NOT EXISTS bidding_hall (
    id                      BIGINT PRIMARY KEY COMMENT '大厅ID',
    auction_id              BIGINT NOT NULL COMMENT '关联竞价单ID',
    hall_status             TINYINT DEFAULT 0 COMMENT '0待开启 1进行中 2已结束',
    bidding_style           VARCHAR(32) DEFAULT 'ENGLISH' COMMENT 'ENGLISH英式/DUTCH荷兰式/JAPANESE日式',
    identity_hidden         TINYINT DEFAULT 0 COMMENT '是否隐藏供应商身份',
    rank_public             TINYINT DEFAULT 1 COMMENT '是否公开排名',
    auto_extend_seconds     INT DEFAULT 0 COMMENT '最后报价自动延长时间(秒)',
    start_time              DATETIME COMMENT '竞价开始时间',
    end_time                DATETIME COMMENT '竞价结束时间',
    created_at              DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at              DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_auction (auction_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞价大厅主表';

-- 竞价报价记录表
CREATE TABLE IF NOT EXISTS bidding_hall_record (
    id                      BIGINT PRIMARY KEY COMMENT '报价记录ID',
    hall_id                 BIGINT NOT NULL COMMENT '大厅ID',
    supplier_id             BIGINT NOT NULL COMMENT '供应商ID',
    bid_amount              DECIMAL(14,2) NOT NULL COMMENT '报价金额',
    rank_position           INT COMMENT '当前排名',
    bid_time                DATETIME(3) DEFAULT CURRENT_TIMESTAMP(3) COMMENT '报价时间(毫秒精度)',
    INDEX idx_hall_time (hall_id, bid_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='竞价报价记录表';
