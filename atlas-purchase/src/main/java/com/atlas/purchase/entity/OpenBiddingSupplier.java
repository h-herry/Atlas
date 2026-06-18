package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 公开招标供应商投标记录实体 / Open bidding supplier bid record entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("open_bidding_supplier")
public class OpenBiddingSupplier {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联招标ID / Associated bidding ID */
    private Long biddingId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 投标报价 / Bid amount */
    private BigDecimal bidAmount;

    /** 投标文件URL / Bid file URL */
    private String bidFileUrl;

    /** 提交时间 / Submission time */
    private LocalDateTime submitTime;

    /** 资格审核: 0-待审核 1-合格 2-不合格 / Qualification: 0-pending 1-qualified 2-disqualified */
    private Integer isQualified;

    /** 评标得分 / Evaluation score */
    private BigDecimal evalScore;

    /** 评标意见 / Evaluation comments */
    private String evalComment;
}
