package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 审批步骤请求 DTO — 审批通过/驳回 /
 * Approval step request DTO — approve/reject
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
public class ApprovalStepRequest {

    /** Flowable 任务ID / Flowable task ID */
    @NotNull(message = "任务ID不能为空 / Task ID is required")
    private String taskId;

    /** 是否通过 / Whether approved */
    private boolean approved;

    /** 审批意见 / Comment */
    private String comment;
}
