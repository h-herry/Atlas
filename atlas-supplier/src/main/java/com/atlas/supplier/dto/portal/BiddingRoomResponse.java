package com.atlas.supplier.dto.portal;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 竞标大厅实时视图响应 DTO — 包含当前排名（匿名）、我的报价、剩余时间、最低价 /
 * Bidding room real-time view response DTO — current ranking (anonymous), my bid, remaining time, lowest price
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
public class BiddingRoomResponse {

    /** 询价单ID / Inquiry ID */
    private Long inquiryId;

    /** 询价单标题 / Inquiry title */
    private String inquiryTitle;

    /** 询价单编号 / Inquiry number */
    private String inquiryNo;

    /** 竞价截止时间 / Bidding deadline */
    private LocalDateTime deadline;

    /** 剩余秒数 / Remaining seconds */
    private Long remainingSeconds;

    /** 竞价状态: OPEN 进行中 / CLOSED 已结束 / Bidding status: OPEN / CLOSED */
    private String status;

    /** 我的当前排名（1-based，1为最低价） / My current rank (1-based, 1 = lowest price) */
    private Integer myRank;

    /** 我最近一次出价 / My last bid price */
    private BigDecimal myLastBid;

    /** 当前最低价（匿名，不暴露供应商名） / Current lowest price (anonymous, supplier identity hidden) */
    private BigDecimal currentLowestPrice;

    /** 参与竞价供应商数（仅显示总数，匿名） / Number of participating suppliers (total count only, anonymous) */
    private Integer bidderCount;

    /** 总出价次数（所有供应商累计） / Total number of bids (cumulative across all suppliers) */
    private Integer totalBids;

    /** 价格排名分布（匿名，仅显示排名序号+价格） / Price ranking distribution (anonymous, rank index + price only) */
    private List<RankingSnapshot> rankingSnapshots;

    // ==================== 内部类 / Inner Class ====================

    /**
     * 排名快照（匿名化，仅显示排名和价格） / Ranking snapshot (anonymized, rank and price only)
     */
    @Data
    public static class RankingSnapshot {

        /** 排名序号 / Rank index */
        private Integer rank;

        /** 报价金额（匿名） / Bid amount (anonymous) */
        private BigDecimal price;

        /** 与上一名价差 / Price gap with previous rank */
        private BigDecimal gapFromPrevious;
    }
}
