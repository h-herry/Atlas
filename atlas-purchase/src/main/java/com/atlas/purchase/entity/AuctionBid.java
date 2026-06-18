package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 竞价出价记录实体 / Auction bid record entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("auction_bid")
public class AuctionBid {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联竞价ID / Associated auction ID */
    private Long auctionId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 出价金额 / Bid amount */
    private BigDecimal bidAmount;

    /** 出价时间 / Bid time */
    private LocalDateTime bidTime;

    /** 是否有效出价 / Is valid bid */
    private Integer isValid;
}
