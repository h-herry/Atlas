package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商注册/准入申请实体 — 对应 supplier_register 表 / Supplier registration / access application entity — maps to supplier_register table
 *
 * @author atlas
 */
@Data
@TableName("supplier_register")
public class SupplierRegister {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联招募公告ID / Associated recruit notice ID */
    private Long recruitNoticeId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 统一社会信用代码 / Unified social credit code */
    private String creditCode;

    /** 法定代表人 / Legal representative */
    private String legalPerson;

    /** 注册资本（万元） / Registered capital (10k CNY) */
    private BigDecimal registeredCapital;

    /** 成立日期 / Established date */
    private LocalDate establishedDate;

    /** 经营范围 / Business scope */
    private String businessScope;

    /** 企业类型 / Company type */
    private String companyType;

    /** 联系人 / Contact person */
    private String contactPerson;

    /** 联系电话 / Contact phone */
    private String contactPhone;

    /** 邮箱 / Email */
    private String email;

    /** 地址 / Address */
    private String address;

    /** 资质文件URL（JSON） / Qualification files URL (JSON) */
    private String qualificationFiles;

    /** 审批状态: 0待审核 1初审通过 2现场考察 3终审通过 4驳回 5已入库 / Approval status: 0=pending, 1=initial passed, 2=site inspection, 3=final approved, 4=rejected, 5=onboarded */
    private Integer approvalStatus;

    /** 驳回原因 / Reject reason */
    private String rejectReason;

    /** 审批人ID / Reviewer ID */
    private Long reviewerId;

    /** 审批人姓名 / Reviewer name */
    private String reviewerName;

    /** 审批时间 / Reviewed time */
    private LocalDateTime reviewedAt;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
