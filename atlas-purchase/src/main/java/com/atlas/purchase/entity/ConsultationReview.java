package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 磋商评审记录实体 / Consultation review record entity
 * <p>
 * 记录竞争性磋商过程中综合评分（技术+商务+价格）及排名结果。 /
 * Records comprehensive scores (tech + business + price) and rankings during competitive consultation.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("consultation_review")
public class ConsultationReview {

    /** 主键ID（雪花算法） / Primary key (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联磋商会话ID / Associated consultation session ID */
    private Long consultationId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 技术得分 / Technical score */
    private BigDecimal techScore;

    /** 商务得分 / Business score */
    private BigDecimal businessScore;

    /** 价格得分 / Price score */
    private BigDecimal priceScore;

    /** 综合得分 / Total score */
    private BigDecimal totalScore;

    /** 最终报价 / Final offer */
    private BigDecimal finalOffer;

    /** 排名 / Rank */
    private Integer rank;

    /** 评审人ID / Reviewer ID */
    private Long reviewerId;

    /** 评审人姓名 / Reviewer name */
    private String reviewerName;

    /** 评审意见 / Review comment */
    private String reviewComment;

    /** 是否中标: 0-否 1-是 / Winner: 0-no 1-yes */
    private Integer isWinner;

    /** 创建时间 / Created time */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
