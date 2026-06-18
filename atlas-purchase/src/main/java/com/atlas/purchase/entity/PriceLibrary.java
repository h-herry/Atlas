package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 价格库实体 / Price library entity
 * <p>
 * 记录各类价格数据：合同价(CONTRACT)、报价(QUOTATION)、现货价(SPOT)、协议价(AGREEMENT)。 /
 * Records various price types: CONTRACT, QUOTATION, SPOT, AGREEMENT.
 * 支持按有效期管理价格生命周期。 / Supports price lifecycle management by validity period.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("price_library")
public class PriceLibrary {

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

    /** 价格类型: CONTRACT / QUOTATION / SPOT / AGREEMENT / Price type: CONTRACT / QUOTATION / SPOT / AGREEMENT */
    private String priceType;

    /** 有效期起始 / Valid from */
    private LocalDate validFrom;

    /** 有效期截止 / Valid to */
    private LocalDate validTo;

    /** 来源订单ID / Source order ID */
    private Long sourceOrderId;

    /** 状态: 1有效 0无效 / Status: 1-active 0-inactive */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
