package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 报价提交请求 DTO / Quotation submit request DTO
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
public class QuotationSubmitRequest {

    /** 报价总金额 / Total quotation amount */
    @NotNull(message = "总金额不能为空 / Total amount is required")
    private BigDecimal totalAmount;

    /** 报价有效期至 / Quotation valid until */
    @NotNull(message = "报价有效期不能为空 / Quotation validity is required")
    private LocalDate validUntil;

    /** 预计交期 / Estimated delivery date */
    @NotNull(message = "预计交期不能为空 / Estimated delivery date is required")
    private LocalDate estimatedDeliveryDate;

    /** 报价备注 / Quotation remarks */
    private String remark;

    /** 物料报价明细 / Material quotation details */
    @NotEmpty(message = "报价明细不能为空 / Quotation details are required")
    private List<QuotationItem> items;

    // ==================== 内部类 / Inner Class ====================

    /**
     * 物料报价明细项 / Material quotation item
     */
    @Data
    public static class QuotationItem {
        /** 物料编号 / Material code */
        @NotNull
        private String materialCode;

        /** 物料名称 / Material name */
        private String materialName;

        /** 单价 / Unit price */
        @NotNull(message = "单价不能为空 / Unit price is required")
        private BigDecimal unitPrice;

        /** 数量 / Quantity */
        @NotNull(message = "数量不能为空 / Quantity is required")
        private Integer quantity;

        /** 小计金额 / Subtotal */
        private BigDecimal subtotal;

        /** 交货期 / Delivery date */
        private LocalDate deliveryDate;

        /** 备注 / Remark */
        private String remark;
    }
}
