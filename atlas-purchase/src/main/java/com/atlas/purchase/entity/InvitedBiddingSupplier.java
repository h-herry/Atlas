package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 邀请招标供应商记录实体 / Invited bidding supplier record entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("invited_bidding_supplier")
public class InvitedBiddingSupplier {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联招标ID / Associated bidding ID */
    private Long biddingId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 邀请状态: 0-已邀请 1-接受 2-拒绝 / Invitation status: 0-invited 1-accepted 2-rejected */
    private Integer inviteStatus;

    /** 投标报价 / Bid amount */
    private BigDecimal bidAmount;

    /** 投标文件URL / Bid file URL */
    private String bidFileUrl;

    /** 提交时间 / Submission time */
    private LocalDateTime submitTime;

    /** 评标得分 / Evaluation score */
    private BigDecimal evalScore;
}
