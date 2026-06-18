package com.atlas.common.template.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 审批流模板实体 — 预置审批链步骤用于供应商准入/订单变更/合同签署 /
 * Approval flow template entity — preset approval chain steps for supplier onboarding / order change / contract signing
 *
 * <p>steps 字段为 JSON 数组: [{"step":1,"role":"DEPT_MGR"},{"step":2,"role":"FINANCE"}] /
 * steps field is a JSON array representing sequential approval steps</p>
 *
 * @since 1.2.22
 */
@Data
@TableName("approval_template")
public class ApprovalTemplate {

    @TableId(type = IdType.ASSIGN_ID)
    private Long templateId;

    private String name;
    private String module;

    /** 审批步骤 JSON: [{"step":1,"role":"DEPT_MGR"}...] / Approval steps */
    private String steps;

    /** 是否默认模板 / Is default template */
    private Integer isDefault;

    /** 状态: ACTIVE / INACTIVE */
    private String status;

    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
