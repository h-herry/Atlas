package com.atlas.contract.econtract.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签署记录实体 — 对应 cnt_sign_record 表 /
 * Sign record entity — corresponds to cnt_sign_record table
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("cnt_sign_record")
public class CntSignRecord {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 签署流程ID / Sign flow ID */
    private Long flowId;

    /** 第几步签署 / Step order */
    private Integer stepOrder;

    /** 签署人类型: INTERNAL/EXTERNAL / Signer type */
    private String signerType;

    /** 签署人ID / Signer ID */
    private Long signerId;

    /** 签署人姓名 / Signer name */
    private String signerName;

    /** 签署人公司 / Signer company */
    private String signerCompany;

    /** 签署状态: PENDING/SIGNED/REJECTED / Sign status */
    private String signStatus;

    /** 签署时间 / Signed at */
    private LocalDateTime signedAt;

    /** 签署IP / Sign IP */
    private String signIp;

    /** 签署方式: SMS_VERIFY/SEAL_IMAGE/HANDWRITE/THIRD_PARTY / Sign method */
    private String signMethod;

    /** 拒签原因 / Reject reason */
    private String rejectReason;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
