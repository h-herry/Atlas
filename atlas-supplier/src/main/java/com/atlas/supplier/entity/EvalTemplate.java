package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 评估模板实体 — 对应 eval_template 表 / Evaluation template entity — maps to eval_template table
 *
 * @author atlas
 */
@Data
@TableName("eval_template")
public class EvalTemplate {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 模板名称 / Template name */
    private String templateName;

    /** 评估类型: 1季度 2年度 3专项 / Evaluation type: 1=quarterly, 2=annual, 3=special */
    private Integer evalType;

    /** 评分维度定义（JSON: [{name, category, maxScore, weight}]） / Scoring dimensions (JSON) */
    private String evalDimensions;

    /** 及格线 / Minimum pass score */
    private BigDecimal minPassScore;

    /** 状态: 1启用 0停用 / Status: 1=enabled, 0=disabled */
    private Integer status;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
