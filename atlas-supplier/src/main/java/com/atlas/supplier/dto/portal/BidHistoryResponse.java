package com.atlas.supplier.dto.portal;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 竞价出价历史响应 DTO — 匿名化展示价格变化曲线 /
 * Bid history response DTO — anonymized price change curve display
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
public class BidHistoryResponse {

    /** 询价单ID / Inquiry ID */
    private Long inquiryId;

    /** 询价单编号 / Inquiry number */
    private String inquiryNo;

    /** 总出价次数 / Total bid count */
    private Integer totalBids;

    /** 参与供应商数 / Number of participating suppliers */
    private Integer bidderCount;

    /** 起始最低价 / Starting lowest price */
    private BigDecimal startingLowestPrice;

    /** 当前最低价 / Current lowest price */
    private BigDecimal currentLowestPrice;

    /** 价格下降幅度 / Price reduction amount */
    private BigDecimal priceReduction;

    /** 出价记录列表（匿名化） / Bid record list (anonymized) */
    private List<BidRecord> records;

    // ==================== 内部类 / Inner Class ====================

    /**
     * 出价记录（匿名化：只显示时间、价格、排名变化） /
     * Bid record (anonymized: only shows time, price, rank change)
     */
    @Data
    public static class BidRecord {

        /** 出价时间 / Bid time */
        private LocalDateTime bidTime;

        /** 出价金额 / Bid amount */
        private BigDecimal price;

        /** 出价后排名 / Rank after this bid */
        private Integer rankAfterBid;

        /** 是否为当前供应商的出价 / Whether this is the current supplier's bid */
        private Boolean isMyBid;

        /** 是否为本轮最低价 / Whether this bid is the current lowest */
        private Boolean isLowest;
    }
}
