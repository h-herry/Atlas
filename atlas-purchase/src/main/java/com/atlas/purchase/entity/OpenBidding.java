package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 公开招标主表实体 / Open bidding master entity
 * <p>
 * 完整招标流程：发布公告→投标→开标→评标→定标。 /
 * Complete bidding lifecycle: publish → bid → open → evaluate → award.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("open_bidding")
public class OpenBidding {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 招标编号 / Bid number */
    private String bidNo;

    /** 关联采购单ID / Associated purchase order ID */
    private Long purchaseOrderId;

    /** 招标项目名称 / Bid project name */
    private String title;

    /** 招标内容/技术要求 / Bid content / technical requirements */
    private String bidContent;

    /** 招标开始日期 / Bid start date */
    private LocalDate bidStartDate;

    /** 投标截止日期 / Bid submission deadline */
    private LocalDate bidEndDate;

    /** 开标日期 / Bid opening date */
    private LocalDate bidOpeningDate;

    /** 预算金额 / Budget amount */
    private BigDecimal budgetAmount;

    /** 投标保证金 / Bid deposit */
    private BigDecimal bidDeposit;

    /** 评标办法: MIN_PRICE/SCORE/BEST_VALUE / Evaluation method */
    private String evaluationMethod;

    /** 状态: 0-准备中 1-公告期 2-投标中 3-开标中 4-评标中 5-定标 6-流标 7-终止 / Status: 0-preparing 1-announcement 2-bidding 3-opening 4-evaluating 5-awarded 6-flowed 7-terminated */
    private Integer status;

    /** 中标供应商ID / Winner supplier ID */
    private Long winnerSupplierId;

    /** 中标金额 / Winner amount */
    private BigDecimal winnerAmount;

    /** 发布人ID / Publisher ID */
    private Long publisherId;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
