package com.atlas.purchase.model;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

/**
 * 创建采购单请求 DTO / Create purchase order request DTO
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Data
public class PurchaseCreateRequest {

    /** 供应商ID / Supplier ID */
    @NotNull(message = "供应商ID不能为空")
    private Long supplierId;

    /** 需求部门ID / Requesting department ID */
    @NotNull(message = "需求部门ID不能为空")
    private Long deptId;

    /** 关联合同ID（可选） / Associated contract ID (optional) */
    private Long contractId;

    /** 创建人ID / Creator ID */
    @NotNull(message = "创建人不能为空")
    private Long createdBy;

    /** 采购方式：1-公开招标 2-邀请招标 3-询比采购 4-竞价采购 5-竞争性谈判 6-竞争性磋商 7-单一来源 8-框架协议 9-合作创新 / Procurement type: 1-open bidding 2-invited bidding 3-inquiry 4-auction 5-negotiation 6-consultation 7-single source 8-framework 9-cooperative innovation */
    private Integer procurementType;

    /** 幂等请求ID / Idempotency request ID */
    @NotBlank(message = "幂等请求ID不能为空")
    private String requestId;

    /** 采购明细列表 / Order item list */
    @NotEmpty(message = "采购明细不能为空")
    @Valid
    private List<OrderItemRequest> items;

    /**
     * 采购明细项 / Order item detail
     */
    @Data
    public static class OrderItemRequest {

        /** SKU ID / SKU ID */
        @NotNull(message = "SKU ID不能为空")
        private Long skuId;

        /** 商品名称 / Product name */
        @NotBlank(message = "商品名称不能为空")
        private String skuName;

        /** 采购数量 / Purchase quantity */
        @NotNull(message = "采购数量不能为空")
        private java.math.BigDecimal quantity;

        /** 单价 / Unit price */
        @NotNull(message = "单价不能为空")
        private java.math.BigDecimal unitPrice;
    }
}
