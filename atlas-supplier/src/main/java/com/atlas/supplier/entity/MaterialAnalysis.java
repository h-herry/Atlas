package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料分析报表实体 / Material analysis report entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("material_analysis")
public class MaterialAnalysis implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 统计周期 / Statistics period */
    private String period;

    /** 库存周转率 / Stock turnover rate */
    private BigDecimal stockTurnoverRate;

    /** 平均库存天数 / Average stock days */
    private BigDecimal avgStockDays;

    /** 呆滞料数量 / Slow-moving quantity */
    private BigDecimal slowMovingQty;

    /** 采购均价 / Average purchase cost */
    private BigDecimal purchaseCostAvg;

    /** 成本趋势: RISE上涨 / FALL下降 / STABLE稳定 / Cost trend */
    private String purchaseCostTrend;

    /** 需求预测 / Demand forecast */
    private BigDecimal demandForecast;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
