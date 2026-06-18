package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 价格走势实体 / Price trend entity
 * <p>
 * 按月维度统计物料价格走势，含均价、最低价、最高价、交易次数和走势方向。 /
 * Monthly price trend statistics including average, min, max price, transaction count and trend direction.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("price_trend")
public class PriceTrend {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 统计周期 (YYYY-MM) / Statistics period (YYYY-MM) */
    private String period;

    /** 平均价格 / Average price */
    private BigDecimal avgPrice;

    /** 最低价格 / Minimum price */
    private BigDecimal minPrice;

    /** 最高价格 / Maximum price */
    private BigDecimal maxPrice;

    /** 交易次数 / Transaction count */
    private Integer transactionCount;

    /** 走势方向: RISE/FALL/STABLE / Trend direction */
    private String trendDirection;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
