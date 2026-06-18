package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 需求预测实体 — 对应 demand_forecast 表 / Demand forecast entity — maps to demand_forecast table
 *
 * <p>采购方录入需求预测，分享给供应商，供应商反馈承诺产能。
 * 置信度 HIGH/MEDIUM/LOW 标识预测可靠程度。 /
 * Buyer enters demand forecasts, shares with suppliers who feedback committed capacity.
 * Confidence levels HIGH/MEDIUM/LOW indicate forecast reliability.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("demand_forecast")
public class DemandForecast {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 预测编号 / Forecast number */
    private String forecastNo;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 预测数量 / Forecast quantity */
    private BigDecimal forecastQty;

    /** 预测月份 (YYYY-MM) / Forecast month */
    private String forecastMonth;

    /** 置信度: HIGH/MEDIUM/LOW / Confidence level */
    private String confidenceLevel;

    /** 来源: SALES预测/PLAN计划/HISTORY历史 / Source: SALES/PLAN/HISTORY */
    private String source;

    /** 是否已分享给供应商 / Shared to supplier */
    private Integer sharedToSupplier;

    /** 供应商承诺量 / Supplier committed quantity */
    private BigDecimal supplierFeedbackQty;

    /** 供应商反馈日期 / Supplier feedback date */
    private LocalDate supplierFeedbackDate;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
