package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 采购明细表实体 / Purchase order item entity
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Data
@TableName("order_item")
public class PurchaseOrderItem {

    /** 明细ID（雪花算法） / Item ID (Snowflake) */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联采购订单ID / Associated order ID */
    private Long orderId;

    /** SKU ID / SKU ID */
    private Long skuId;

    /** 商品名称 / Product name */
    private String skuName;

    /** 采购数量 / Purchase quantity */
    private BigDecimal quantity;

    /** 单价 / Unit price */
    private BigDecimal unitPrice;

    /** 小计 / Subtotal */
    private BigDecimal totalPrice;
}
