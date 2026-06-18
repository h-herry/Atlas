package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评估明细项实体 — 对应 supplier_evaluation_item 表 / Evaluation line item entity — maps to supplier_evaluation_item table
 *
 * @author atlas
 */
@Data
@TableName("supplier_evaluation_item")
public class SupplierEvaluationItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 评估主表ID / Evaluation ID */
    private Long evaluationId;

    /** 考核项名称 / Item name */
    private String itemName;

    /** 类别: QUALITY / DELIVERY / COST / SERVICE / Category */
    private String itemCategory;

    /** 权重(%) / Weight (%) */
    private BigDecimal weight;

    /** 满分 / Maximum score */
    private BigDecimal maxScore;

    /** 实际得分 / Actual score */
    private BigDecimal actualScore;

    /** 数据来源: ERP / MES / MANUAL / Score source */
    private String scoreSource;

    /** 评分依据 / Data evidence */
    private String dataEvidence;

    /** 备注 / Remark */
    private String remark;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
