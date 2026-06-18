package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 交期承诺请求 DTO — 供应商向采购企业承诺具体交期 /
 * Delivery commitment request DTO — supplier commits specific delivery date to purchasing enterprise
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
public class DeliveryCommitmentRequest {

    /** 承诺交付日期 / Committed delivery date */
    @NotNull(message = "承诺交期不能为空 / Committed delivery date is required")
    private LocalDate committedDate;

    /** 生产计划说明 / Production plan description */
    @NotBlank(message = "生产计划说明不能为空 / Production plan description is required")
    private String productionPlan;

    /** 产能确认说明 / Capacity confirmation note */
    private String capacityConfirmation;

    /** 备注说明 / Remark */
    private String remark;
}
