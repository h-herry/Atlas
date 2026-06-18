package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 框架协议供应商实体 / Framework agreement supplier entity
 * <p>
 * 记录框架协议中入围供应商的折扣率、单次上限及状态。 /
 * Records shortlisted supplier discount rate, single-order cap and status within the framework agreement.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("framework_supplier")
public class FrameworkSupplier {

    /** 主键ID（雪花算法） / Primary key (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联框架协议ID / Associated agreement ID */
    private Long agreementId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 约定折扣率(%) / Agreed discount rate (%) */
    private BigDecimal agreedDiscount;

    /** 单次上限金额 / Single order maximum amount */
    private BigDecimal maxSingleAmount;

    /** 入围排名 / Shortlist rank */
    private Integer rank;

    /** 状态: 1-入围 2-暂停 3-退出 / Status: 1-shortlisted 2-suspended 3-exited */
    private Integer status;

    /** 入围日期 / Joined date */
    private LocalDateTime joinedAt;
}
