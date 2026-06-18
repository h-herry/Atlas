package com.atlas.contract.econtract.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 签署流程实体 — 对应 cnt_sign_flow 表 /
 * Sign flow entity — corresponds to cnt_sign_flow table
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("cnt_sign_flow")
public class CntSignFlow {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联合同ID / Contract ID */
    private Long contractId;

    /** 签署类型: ONLINE/OFFLINE / Sign type */
    private String signType;

    /** 签署状态: DRAFT/SIGNING/COMPLETED/EXPIRED/CANCELLED / Sign status */
    private String status;

    /** 发起人ID / Initiator ID */
    private Long initiatorId;

    /** 当前步骤 / Current step */
    private Integer currentStep;

    /** 总步骤数 / Total steps */
    private Integer totalSteps;

    /** 签署截止时间 / Sign deadline */
    private LocalDateTime signDeadline;

    /** 完成时间 / Completed at */
    private LocalDateTime completedAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
