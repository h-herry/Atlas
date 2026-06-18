package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 发货状态更新请求 DTO / Delivery status update request DTO
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
public class DeliveryUpdateRequest {

    /** 物流公司名称 / Logistics company name */
    @NotBlank(message = "物流公司不能为空 / Logistics company is required")
    private String logisticsCompany;

    /** 物流单号 / Tracking number */
    @NotBlank(message = "物流单号不能为空 / Tracking number is required")
    private String trackingNo;

    /** 预计到达时间 / Estimated arrival time */
    @NotNull(message = "预计到达时间不能为空 / Estimated arrival time is required")
    private LocalDateTime estimatedArrival;

    /** 发货备注 / Shipping remarks */
    private String remark;
}
