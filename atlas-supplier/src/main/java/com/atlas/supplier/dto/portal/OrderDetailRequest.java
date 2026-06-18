package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * 订单明细填写请求 DTO — 供应商收到采购单据后补充细节 /
 * Order detail fill request DTO — supplier supplements order details after receiving purchase document
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
public class OrderDetailRequest {

    /** 物料规格 / Material specification */
    @NotBlank(message = "物料规格不能为空 / Material specification is required")
    private String materialSpec;

    /** 单价 / Unit price */
    @NotNull(message = "单价不能为空 / Unit price is required")
    @Positive(message = "单价必须大于0 / Unit price must be positive")
    private BigDecimal unitPrice;

    /** 预计交期 / Estimated delivery date */
    @NotNull(message = "预计交期不能为空 / Estimated delivery date is required")
    private LocalDate estimatedDeliveryDate;

    /** 生产批次号 / Production batch number */
    private String productionBatch;

    /** 最小起订量 / Minimum order quantity */
    @Positive(message = "最小起订量必须大于0 / Minimum order quantity must be positive")
    private Integer minOrderQuantity;

    /** 生产周期（天） / Lead time (days) */
    @Positive(message = "生产周期必须大于0 / Lead time must be positive")
    private Integer leadTimeDays;

    /** 包装方式 / Packaging type */
    private String packagingType;

    /** 备注说明 / Remark */
    private String remark;
}
