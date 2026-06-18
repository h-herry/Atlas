package com.atlas.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 合同到期提醒实体 — 每日扫描到期前 30/15/7/1 天的合同 /
 * Contract expiry alert entity — daily scan for contracts expiring in 30/15/7/1 days
 *
 * <p>推送提醒至消息中心（合同负责人 + 供应商），到期自动标记 EXPIRED /
 * Push alerts to message center (contract owner + supplier); auto-mark EXPIRED upon expiry</p>
 *
 * @since 1.2.22
 */
@Data
@TableName("contract_alert")
public class ContractAlert {

    @TableId(type = IdType.ASSIGN_ID)
    private Long alertId;

    private Long contractId;
    private String contractNo;

    /** 提醒类型: EXPIRY_90 / EXPIRY_60 / EXPIRY_30 / EXPIRY_7 / EXPIRY_1 / EXPIRED */
    private String alertType;

    private LocalDate expireDate;
    private LocalDate alertDate;

    /** 已通知合同负责人 / Owner notified */
    private Integer notifiedOwner;

    /** 已通知供应商 / Supplier notified */
    private Integer notifiedSupplier;

    /** 状态: PENDING / SENT / READ */
    private String status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
