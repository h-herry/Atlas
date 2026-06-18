package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商审批记录实体 — 对应 supplier_approval_record 表 / Supplier approval record entity — maps to supplier_approval_record table
 *
 * @author atlas
 */
@Data
@TableName("supplier_approval_record")
public class SupplierApprovalRecord {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 注册申请ID / Registration ID */
    private Long registerId;

    /** 审批节点: INITIAL_REVIEW / FIELD_INSPECT / FINAL_REVIEW / Approval node */
    private String approvalNode;

    /** 审批结果: 1通过 2驳回 3退回修改 / Approval result: 1=approved, 2=rejected, 3=return for revision */
    private Integer approvalResult;

    /** 评分 / Score */
    private BigDecimal score;

    /** 审批意见 / Comment */
    private String comment;

    /** 审批人ID / Approver ID */
    private Long approverId;

    /** 审批人姓名 / Approver name */
    private String approverName;

    /** 审批部门: QUALITY / TECH / FINANCE / Approver department */
    private String approverDept;

    /** 审批时间 / Approved time */
    private LocalDateTime approvedAt;
}
