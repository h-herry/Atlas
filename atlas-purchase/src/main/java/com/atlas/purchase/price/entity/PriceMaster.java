package com.atlas.purchase.price.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 价格主数据实体 / Price master data entity
 *
 * <p>管理物料-供应商维度的标准化价格，支持有效期管理和来源追溯。 /
 * Manages standardized material-supplier price with validity period and source traceability.
 * 来源标记: MANUAL / SETTLEMENT / CONTRACT</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("price_master")
public class PriceMaster {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 单价 / Unit price */
    private BigDecimal unitPrice;

    /** 币种，默认 CNY / Currency, default CNY */
    private String currency;

    /** 生效日期 / Effective date */
    private LocalDate effectiveDate;

    /** 失效日期 / Expiration date */
    private LocalDate expireDate;

    /** 来源: MANUAL / SETTLEMENT / CONTRACT */
    private String source;

    /** 状态: 1有效 0无效 / Status: 1-active 0-inactive */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
