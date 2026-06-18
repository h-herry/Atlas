package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 竞价提交请求 DTO — 供应商在竞标大厅提交竞价报价 /
 * Bid submission request DTO — supplier submits bid in bidding room
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
public class BidRequest {

    /** 竞价总价 / Total bid price */
    @NotNull(message = "总价不能为空 / Total price is required")
    @Positive(message = "总价必须大于0 / Total price must be positive")
    private BigDecimal totalPrice;

    /** 物料报价明细 / Material bid details */
    @NotEmpty(message = "物料明细不能为空 / Material details are required")
    private List<BidItem> items;

    /** 交期天数 / Delivery days */
    @Positive(message = "交期天数必须大于0 / Delivery days must be positive")
    private Integer deliveryDays;

    /** 报价有效期至 / Bid valid until */
    @NotNull(message = "报价有效期不能为空 / Bid valid until is required")
    private LocalDate validUntil;

    /** 报价备注 / Bid remark */
    private String remark;

    // ==================== 内部类 / Inner Class ====================

    /**
     * 竞价物料报价项 / Bid material item
     */
    @Data
    public static class BidItem {

        /** 物料ID / Material ID */
        @NotNull(message = "物料ID不能为空 / Material ID is required")
        private Long materialId;

        /** 单价 / Unit price */
        @NotNull(message = "单价不能为空 / Unit price is required")
        @Positive(message = "单价必须大于0 / Unit price must be positive")
        private BigDecimal unitPrice;

        /** 数量 / Quantity */
        @NotNull(message = "数量不能为空 / Quantity is required")
        @Positive(message = "数量必须大于0 / Quantity must be positive")
        private Integer quantity;

        /** 物料名称（可选） / Material name (optional) */
        private String materialName;
    }
}
