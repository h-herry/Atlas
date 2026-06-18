package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商评分卡明细项实体 — 对应 supplier_scorecard_item 表 /
 * Supplier scorecard line item entity — maps to supplier_scorecard_item table
 *
 * <p>记录各维度评分明细，维度包括 QUALITY / COST / DELIVERY / SERVICE。 /
 * Stores dimension scoring details: QUALITY / COST / DELIVERY / SERVICE.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("supplier_scorecard_item")
public class SupplierScorecardItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联评分卡ID / Associated scorecard ID */
    private Long scorecardId;

    /** 维度: QUALITY/COST/DELIVERY/SERVICE / Dimension */
    private String dimension;

    /** 评分项名称 / Item name */
    private String itemName;

    /** 权重(%) / Weight (%) */
    private BigDecimal weight;

    /** 满分 / Maximum score */
    private BigDecimal maxScore;

    /** 实际得分 / Actual score */
    private BigDecimal actualScore;

    /** 数据来源 / Data source */
    private String dataSource;

    /** 备注 / Remark */
    private String remark;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
