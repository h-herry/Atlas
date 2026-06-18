package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 谈判报价记录实体 / Negotiation round record entity
 * <p>
 * 记录竞争性谈判过程中每一轮每位供应商的报价与技术方案。 /
 * Records each round's quotes and technical proposals from each supplier during competitive negotiation.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("negotiation_round")
public class NegotiationRound {

    /** 主键ID（雪花算法） / Primary key (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联谈判会话ID / Associated negotiation session ID */
    private Long negotiationId;

    /** 轮次 / Round number */
    private Integer roundNo;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 报价金额 / Quoted amount */
    private BigDecimal offerAmount;

    /** 技术方案 / Technical proposal */
    private String techProposal;

    /** 交货天数 / Delivery days */
    private Integer deliveryDays;

    /** 付款条件 / Payment terms */
    private String paymentTerms;

    /** 谈判记录/评审意见 / Negotiation record / review comments */
    private String negotiatorComment;

    /** 是否最终报价: 0-否 1-是 / Final offer: 0-no 1-yes */
    private Integer isFinal;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
