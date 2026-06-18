package com.atlas.supplier.dto.portal;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 比价结果响应 DTO — 揭标后供应商可查看各供应商报价对比（供应商名匿名化） /
 * Comparison result response DTO — after opening, suppliers can view bid comparison (supplier names anonymized)
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
public class ComparisonResultResponse {

    /** 询价单ID / Inquiry ID */
    private Long inquiryId;

    /** 询价单编号 / Inquiry number */
    private String inquiryNo;

    /** 询价单标题 / Inquiry title */
    private String inquiryTitle;

    /** 定标状态: AWARDED 已定标 / PENDING 待定标 / Award status */
    private String awardStatus;

    /** 揭标时间 / Opening time */
    private LocalDateTime openedAt;

    /** 参与供应商总数 / Total participating suppliers */
    private Integer totalSuppliers;

    /** 供应商排名列表（匿名化） / Supplier ranking list (anonymized) */
    private List<SupplierRanking> rankings;

    /** 中标供应商匿名代号 / Awarded supplier anonymous code */
    private String awardedSupplierCode;

    // ==================== 内部类 / Inner Class ====================

    /**
     * 供应商排名项（匿名化展示） / Supplier ranking item (anonymized display)
     */
    @Data
    public static class SupplierRanking {

        /** 排名 / Rank */
        private Integer rank;

        /** 供应商匿名代号 / Supplier anonymous code */
        private String supplierCode;

        /** 总报价金额 / Total bid amount */
        private BigDecimal totalPrice;

        /** 交期天数 / Delivery days */
        private Integer deliveryDays;

        /** 报价时间 / Bid time */
        private LocalDateTime bidTime;

        /** 与最低价差 / Price gap from lowest */
        private BigDecimal gapFromLowest;

        /** 是否为当前登录供应商 / Whether this is the current logged-in supplier */
        private Boolean isCurrentSupplier;

        /** 是否中标 / Whether awarded */
        private Boolean isAwarded;
    }
}
