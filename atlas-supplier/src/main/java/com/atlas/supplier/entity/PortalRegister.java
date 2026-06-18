package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商入驻申请表实体 — 对应 sup_portal_register 表，支持自助注册+采购员代注册双通道 /
 * Supplier onboarding application entity — maps to sup_portal_register table, supports self-registration + purchaser proxy dual channel
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
@TableName("sup_portal_register")
public class PortalRegister {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 公司名称 / Company name */
    private String companyName;

    /** 统一社会信用代码 / Unified social credit code */
    private String creditCode;

    /** 法定代表人 / Legal representative */
    private String legalPerson;

    /** 联系人姓名 / Contact name */
    private String contactName;

    /** 联系人电话 / Contact phone */
    private String contactPhone;

    /** 联系人邮箱 / Contact email */
    private String contactEmail;

    /** 行业类别 / Industry category */
    private String industryCategory;

    /** 主营产品 / Main products */
    private String mainProducts;

    /** 年营收（元） / Annual revenue (CNY) */
    private BigDecimal annualRevenue;

    /** 员工人数 / Employee count */
    private Integer employeeCount;

    /** 资质证书列表（JSON数组） / Certificate list (JSON array) */
    private String certificates;

    /** 来源：SELF-自助注册 / PURCHASER-采购员代注册 / Channel: SELF=self-registration, PURCHASER=proxy by purchaser */
    private String source;

    /** 发起人ID（采购员代注册时） / Initiator ID (for purchaser proxy registration) */
    private Long initiatorId;

    /** 发起人姓名 / Initiator name */
    private String initiatorName;

    /** Flowable流程实例ID / Flowable process instance ID */
    private String processInstanceId;

    /** 申请状态: 0待审批 1审批中 2已通过 3已驳回 4已撤回 / Status: 0=pending, 1=in review, 2=approved, 3=rejected, 4=withdrawn */
    private Integer applyStatus;

    /** 审批通过后关联的供应商主数据ID / Supplier master data ID after approval */
    private Long supplierId;

    /** 驳回原因 / Reject reason */
    private String rejectReason;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
