package com.atlas.supplier.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入驻进度查询响应 DTO — 审批流转状态 /
 * Registration status query response DTO — approval flow status
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterStatusResponse {

    /** 申请ID / Application ID */
    private Long applyId;

    /** 公司名称 / Company name */
    private String companyName;

    /** 申请状态: 0待审批 1审批中 2已通过 3已驳回 4已撤回 / Status: 0=pending, 1=in review, 2=approved, 3=rejected, 4=withdrawn */
    private Integer applyStatus;

    /** 状态描述 / Status description */
    private String statusDesc;

    /** 当前审批节点 / Current approval node */
    private String currentNode;

    /** Flowable 流程实例ID / Flowable process instance ID */
    private String processInstanceId;

    /** 审批历史 / Approval history */
    private List<ApprovalNodeInfo> approvalHistory;

    /** 驳回原因（驳回时） / Reject reason (when rejected) */
    private String rejectReason;

    /** 供应商ID（通过后） / Supplier ID (after approval) */
    private Long supplierId;

    /** 提交时间 / Submission time */
    private LocalDateTime createdAt;

    // ==================== 内部类 / Inner Classes ====================

    /**
     * 审批节点信息 / Approval node info
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApprovalNodeInfo {

        /** 审批节点名称 / Approval node name */
        private String nodeName;

        /** 审批结果: 1通过 2驳回 / Result: 1=approved, 2=rejected */
        private Integer result;

        /** 审批意见 / Comment */
        private String comment;

        /** 审批人姓名 / Approver name */
        private String approverName;

        /** 审批时间 / Approval time */
        private LocalDateTime approvedAt;
    }
}
