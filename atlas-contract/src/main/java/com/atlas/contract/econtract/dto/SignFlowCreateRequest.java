package com.atlas.contract.econtract.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 签署流程创建请求 / Sign flow create request
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
public class SignFlowCreateRequest {

    /** 合同ID / Contract ID */
    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    /** 签署类型: ONLINE/OFFLINE / Sign type */
    @NotNull(message = "签署类型不能为空")
    private String signType;

    /** 签署截止时间 / Sign deadline */
    private LocalDateTime signDeadline;

    /** 签署人列表 / Signer list */
    @NotNull(message = "签署人列表不能为空")
    private List<SignerInfo> signers;

    /**
     * 签署人信息 / Signer info
     */
    @Data
    public static class SignerInfo {
        /** 签署人类型: INTERNAL/EXTERNAL / Signer type */
        private String signerType;

        /** 签署人ID / Signer ID */
        private Long signerId;

        /** 签署人姓名 / Signer name */
        private String signerName;

        /** 签署人公司 / Signer company */
        private String signerCompany;

        /** 签署方式 / Sign method */
        private String signMethod;
    }
}
