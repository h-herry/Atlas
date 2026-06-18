package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 交期延迟报备请求 DTO — 供应商因故无法按期交付时向企业报备 /
 * Delivery delay report request DTO — supplier reports to enterprise when unable to deliver on time
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
public class DelayReportRequest {

    /** 延迟原因 / Delay reason */
    @NotBlank(message = "延迟原因不能为空 / Delay reason is required")
    private String delayReason;

    /** 新的预计交期 / New estimated delivery date */
    @NotNull(message = "新预计交期不能为空 / New estimated delivery date is required")
    private LocalDate newEstimatedDate;

    /** 影响范围说明 / Impact description */
    @NotBlank(message = "影响说明不能为空 / Impact description is required")
    private String impactDescription;

    /** 补救措施 / Remedial measures */
    private String remedialMeasures;

    /** 延迟天数 / Delay days (calculated or manually specified) */
    private Integer delayDays;
}
