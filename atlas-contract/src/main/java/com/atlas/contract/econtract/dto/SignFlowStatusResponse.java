package com.atlas.contract.econtract.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 签署流程状态响应 / Sign flow status response
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignFlowStatusResponse {

    /** 签署流程ID / Flow ID */
    private Long flowId;

    /** 合同ID / Contract ID */
    private Long contractId;

    /** 签署类型 / Sign type */
    private String signType;

    /** 当前状态 / Current status */
    private String status;

    /** 当前步骤 / Current step */
    private Integer currentStep;

    /** 总步骤数 / Total steps */
    private Integer totalSteps;

    /** 签署截止时间 / Sign deadline */
    private LocalDateTime signDeadline;

    /** 签署记录列表 / Sign record list */
    private List<SignRecordItem> records;

    /**
     * 签署记录项 / Sign record item
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignRecordItem {
        /** 步骤序号 / Step order */
        private Integer stepOrder;

        /** 签署人姓名 / Signer name */
        private String signerName;

        /** 签署人公司 / Signer company */
        private String signerCompany;

        /** 签署状态 / Sign status */
        private String signStatus;

        /** 签署时间 / Signed at */
        private LocalDateTime signedAt;

        /** 签署方式 / Sign method */
        private String signMethod;

        /** 拒签原因 / Reject reason */
        private String rejectReason;
    }
}
