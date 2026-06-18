package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 预测计划实体 — 对应 forecast_notice 表 / Forecast notice entity — maps to forecast_notice table
 *
 * @author atlas
 */
@Data
@TableName("forecast_notice")
public class ForecastNotice {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 预测周期: 2026-06 / Forecast period */
    private String forecastPeriod;

    /** 物料名称 / Material name */
    private String materialName;

    /** 预测数量 / Forecast quantity */
    private Integer forecastQty;

    /** 单位 / Unit */
    private String unit;

    /** 置信度: HIGH / MEDIUM / LOW / Confidence level */
    private String confidenceLevel;

    /** 备注 / Remark */
    private String remark;

    /** 发布人ID / Publisher ID */
    private Long publisherId;

    /** 发布人姓名 / Publisher name */
    private String publisherName;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
