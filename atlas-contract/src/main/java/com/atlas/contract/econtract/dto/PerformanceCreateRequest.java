package com.atlas.contract.econtract.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 履约指标创建请求 / Performance metric create request
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
public class PerformanceCreateRequest {

    /** 合同ID / Contract ID */
    @NotNull(message = "合同ID不能为空")
    private Long contractId;

    /** 条款引用(如 第3.2条) / Clause reference (e.g. Art 3.2) */
    private String clauseRef;

    /** 履约指标名称 / Performance metric name */
    @NotBlank(message = "履约指标名称不能为空")
    private String metricName;

    /** 指标类型 / Metric type */
    @NotBlank(message = "指标类型不能为空")
    private String metricType;

    /** 目标值 / Target value */
    private String targetValue;

    /** 实际值 / Actual value */
    private String actualValue;

    /** 到期日期 / Due date */
    private LocalDate dueDate;

    /** 备注 / Remark */
    private String remark;
}
