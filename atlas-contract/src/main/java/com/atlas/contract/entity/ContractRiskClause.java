package com.atlas.contract.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合同风险条款实体 — 对应 contract_risk_clause 表 /
 * Contract risk clause entity — corresponds to contract_risk_clause table
 *
 * <p>记录合同中的风险条款，支持四种风险类型：
 * PRICE(价格风险)、DELIVERY(交付风险)、PAYMENT(付款风险)、LEGAL(法律风险)。
 * 通过预设风险关键词自动匹配或人工标注，联动审批流。 /
 * Records risk clauses in contracts, supporting four risk types:
 * PRICE, DELIVERY, PAYMENT, LEGAL.
 * Auto-matched via preset risk keywords or manually annotated, linked to approval workflow.
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("contract_risk_clause")
public class ContractRiskClause {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 合同ID / Contract ID */
    private Long contractId;

    /** 风险条款原文 / Original risk clause text */
    private String clauseContent;

    /** 风险类型: PRICE/DELIVERY/PAYMENT/LEGAL / Risk type */
    private String riskType;

    /** 风险等级: HIGH/MEDIUM/LOW / Risk level */
    private String riskLevel;

    /** 修改建议 / Modification suggestion */
    private String suggestion;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
