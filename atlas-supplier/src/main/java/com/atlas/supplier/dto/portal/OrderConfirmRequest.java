package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 订单确认/拒绝请求 DTO / Order confirm/reject request DTO
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
public class OrderConfirmRequest {

    /** 操作类型: CONFIRM 确认接单 / REJECT 拒绝接单 / Operation type */
    @NotBlank(message = "操作类型不能为空 / Operation type is required")
    private String action;

    /** 拒绝原因（action=REJECT 时必填） / Reject reason (required when action=REJECT) */
    private String rejectReason;

    /** 预计交期确认（action=CONFIRM 时可选） / Confirmed delivery date (optional when action=CONFIRM) */
    private String confirmedDeliveryDate;
}
