package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 邀请招标主表实体 / Invited bidding master entity
 * <p>
 * 向特定供应商发出邀请，被邀请方接受后参与投标。 /
 * Invites specific suppliers to participate in bidding after acceptance.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("invited_bidding")
public class InvitedBidding {

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

    /** 投标截止日期 / Bid submission deadline */
    private LocalDate bidEndDate;

    /** 开标日期 / Bid opening date */
    private LocalDate bidOpeningDate;

    /** 预算金额 / Budget amount */
    private BigDecimal budgetAmount;

    /** 采用邀请招标理由 / Reason for invited bidding */
    private String invitationReason;

    /** 最少邀请数量 / Minimum invite count */
    private Integer minInviteCount;

    /** 状态: 0-准备中 1-邀请中 2-投标中 3-开标 4-评标 5-定标 6-终止 / Status: 0-preparing 1-inviting 2-bidding 3-opening 4-evaluating 5-awarded 6-terminated */
    private Integer status;

    /** 中标供应商ID / Winner supplier ID */
    private Long winnerSupplierId;

    /** 中标金额 / Winner amount */
    private BigDecimal winnerAmount;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
