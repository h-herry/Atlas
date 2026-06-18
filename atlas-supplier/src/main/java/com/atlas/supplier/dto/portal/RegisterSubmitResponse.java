package com.atlas.supplier.dto.portal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 入驻申请提交响应 DTO — 返回申请ID和流程实例ID /
 * Onboarding application submit response DTO — returns application ID and process instance ID
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterSubmitResponse {

    /** 入驻申请ID / Application ID */
    private Long applyId;

    /** Flowable 流程实例ID / Flowable process instance ID */
    private String processInstanceId;

    /** 申请状态 / Application status */
    private Integer applyStatus;

    /** 提示消息 / Tip message */
    private String message;
}
