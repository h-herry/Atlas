package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

/**
 * 生产进度更新请求 DTO — 供应商上报订单生产进度 /
 * Production progress update request DTO — supplier reports order production progress
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
public class ProductionProgressRequest {

    /** 完成百分比（0-100） / Completion percentage (0-100) */
    @NotNull(message = "完成百分比不能为空 / Completion percentage is required")
    @Min(value = 0, message = "完成百分比不能小于0 / Progress percentage must be >= 0")
    @Max(value = 100, message = "完成百分比不能大于100 / Progress percentage must be <= 100")
    private Integer progressPercent;

    /** 当前生产阶段 / Current production stage */
    @NotBlank(message = "当前阶段不能为空 / Current stage is required")
    private String currentStage;

    /** 预计完成日期 / Estimated completion date */
    @NotNull(message = "预计完成日期不能为空 / Estimated completion date is required")
    private LocalDate estimatedCompleteDate;

    /** 质检是否通过（可选） / Quality check passed (optional) */
    private Boolean qualityCheckPassed;

    /** 备注说明 / Remark */
    private String remark;
}
