package com.atlas.purchase.inquiry.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 报价明细实体 / Quote item entity
 *
 * <p>供应商针对每个询价行项目的逐项报价，包含单价和承诺交期。 /
 * Supplier's line-item quote for each inquiry item, containing unit price and delivery days.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("quote_item")
public class QuoteItem {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联报价ID / Associated quote ID */
    private Long quoteId;

    /** 关联询价行项目ID / Associated inquiry item ID */
    private Long inquiryItemId;

    /** 单价 / Unit price */
    private BigDecimal unitPrice;

    /** 承诺交期（天） / Delivery days committed */
    private Integer deliveryDays;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
