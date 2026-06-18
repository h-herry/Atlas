package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 供应商智能推荐实体 / Supplier smart recommendation entity
 * <p>
 * 按物料品类自动匹配推荐供应商，支持四种推荐维度： /
 * Auto-match and recommend suppliers by material category, supports four dimensions:
 * HISTORY(历史交易)、CATEGORY(同类品类)、REGION(同区域)、CERT(资质匹配)。 /
 * HISTORY (past transactions), CATEGORY (same category), REGION (same region), CERT (certification match).</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("supplier_recommendation")
public class SupplierRecommendation {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 推荐供应商ID / Recommended supplier ID */
    private Long recommendedSupplierId;

    /** 推荐类型: HISTORY / CATEGORY / REGION / CERT / Recommendation type: HISTORY / CATEGORY / REGION / CERT */
    private String recommendationType;

    /** 匹配度 (0-100) / Match score (0-100) */
    private BigDecimal matchScore;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
