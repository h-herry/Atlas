package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商黑名单实体 — 对应 supplier_blacklist 表 / Supplier blacklist entity — maps to supplier_blacklist table
 *
 * @author atlas
 */
@Data
@TableName("supplier_blacklist")
public class SupplierBlacklist {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 拉黑原因 / Blacklist reason */
    private String blackReason;

    /** 拉黑类型: PERMANENT永久 / TEMPORARY临时 / Blacklist type */
    private String blackType;

    /** 生效日期 / Effective date */
    private LocalDate effectiveDate;

    /** 过期日期（临时拉黑用） / Expiry date (for temporary blacklist) */
    private LocalDate expireDate;

    /** 操作人ID / Operator ID */
    private Long operatorId;

    /** 操作人姓名 / Operator name */
    private String operatorName;

    /** 状态: 1生效 0解除 / Status: 1=active, 0=removed */
    private Integer status;

    /** 风险来源: CREDIT征信/OPINION舆情/INTERNAL内部 / Risk source */
    private String riskSource;

    /** 风险等级: HIGH/MEDIUM/LOW / Risk level */
    private String riskLevel;

    /** 监控到期日 / Monitoring expiry date */
    private LocalDate monitorExpireDate;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
