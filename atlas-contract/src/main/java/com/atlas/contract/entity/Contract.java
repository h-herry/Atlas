package com.atlas.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同实体 — 含状态机驱动字段 / Contract entity — includes state-machine-driven fields
 */
@Data
@TableName("contract")
public class Contract {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    private String contractNo;
    private String contractName;
    private Long supplierId;
    private String supplierName;
    private Long deptId;
    private String deptName;
    private Long creatorId;
    private String creatorName;

    /** 合同类型: 1-采购 2-服务 3-工程 / Contract type: 1-procurement 2-service 3-engineering */
    private Integer contractType;

    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal remainingAmount;
    private LocalDate signDate;
    private LocalDate startDate;
    private LocalDate endDate;

    /** 状态机当前状态: 0保存 1提交 2审核中 3审核通过 4驳回 5已签署 6执行中 7变更中 8已终止 9已完成 /
     *  State machine current status: 0-draft 1-submitted 2-under-review 3-approved 4-rejected 5-signed 6-executing 7-under-change 8-terminated 9-completed */
    private Integer status;

    /** 签章状态: 0未签 1采购方已签 2供应商已签 3双方已签 / Sign status: 0-unsigned 1-buyer-signed 2-supplier-signed 3-both-signed */
    private Integer signStatus;

    /** 已签章合同文件URL / Signed contract file URL */
    private String signedContractUrl;

    private String rejectReason;
    private Integer version;

    // ---- P1-3.8.2 框架合同+执行合同扩展 / Framework + Execution contract extension ----

    /** 合同结构分类: FRAMEWORK框架/EXECUTION执行/STANDALONE独立 /
     * Contract category: FRAMEWORK/EXECUTION/STANDALONE */
    private String contractCategory;

    /** 父合同ID（执行合同关联框架合同）/ Parent contract ID (execution links to framework) */
    private Long parentContractId;

    /** 框架合同金额上限 / Framework max amount */
    private BigDecimal frameworkMaxAmount;

    /** 框架合同数量上限 / Framework max quantity */
    private BigDecimal frameworkMaxQty;

    /** 已执行金额 / Consumed amount */
    private BigDecimal frameworkConsumedAmount;

    /** 已执行数量 / Consumed quantity */
    private BigDecimal frameworkConsumedQty;

    /** 自动续约标记: 0否 1是 / Auto-renewal flag: 0-no 1-yes */
    private Integer autoRenewal;

    /** 续约提前通知天数 / Renewal notice days */
    private Integer renewalNoticeDays;

    /** 合同到期日期（冗余，加速查询）/ Expiry date (redundant for query speed) */
    private LocalDate expiredAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
