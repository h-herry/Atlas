package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 入驻审批记录实体 — 对应 sup_onboarding_approval 表 /
 * Onboarding approval record entity — maps to sup_onboarding_approval table
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
@TableName("sup_onboarding_approval")
public class OnboardingApproval {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 入驻申请ID / Registration application ID */
    private Long registerId;

    /** Flowable任务ID / Flowable task ID */
    private String taskId;

    /** 审批节点: INITIAL_REVIEW / FIELD_INSPECT / FINAL_REVIEW / Approval node */
    private String approvalNode;

    /** 审批结果: 1通过 2驳回 / Approval result: 1=approved, 2=rejected */
    private Integer approvalResult;

    /** 审批意见 / Approval comment */
    private String comment;

    /** 审批人ID / Approver ID */
    private Long approverId;

    /** 审批人姓名 / Approver name */
    private String approverName;

    /** 审批人角色：PURCHASE_SUPERVISOR / QC_INSPECTOR / PURCHASE_DIRECTOR / Approver role */
    private String approverRole;

    /** 审批时间 / Approval time */
    private LocalDateTime approvedAt;

    /** 记录创建时间 / Record created time */
    private LocalDateTime createdAt;
}
