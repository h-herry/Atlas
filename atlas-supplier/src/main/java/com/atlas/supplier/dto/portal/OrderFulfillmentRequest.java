package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单履行状态更新请求 DTO / Order fulfillment status update request DTO
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
public class OrderFulfillmentRequest {

    /** 履行状态: PRODUCING 生产中 / PARTIAL_SHIPPED 已部分发货 / FULL_SHIPPED 已全部发货 /
     * Fulfillment status: PRODUCING / PARTIAL_SHIPPED / FULL_SHIPPED */
    @NotBlank(message = "履行状态不能为空 / Fulfillment status is required")
    private String fulfillmentStatus;

    /** 完成百分比（0-100） / Completion percentage (0-100) */
    @NotNull(message = "完成百分比不能为空 / Completion percentage is required")
    private Integer completionPercent;

    /** 已发货数量 / Shipped quantity */
    private Integer shippedQuantity;

    /** 预计完成时间 / Estimated completion time */
    private LocalDateTime estimatedCompletion;

    /** 备注说明 / Remark */
    private String remark;
}
