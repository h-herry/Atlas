package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商发货明细实体 / Supplier delivery line item entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("supplier_delivery_item")
public class SupplierDeliveryItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 发货单ID / Delivery ID */
    private Long deliveryId;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 发货数量 / Delivery quantity */
    private BigDecimal deliveryQty;

    /** 生产批次号 / Production batch number */
    private String batchNo;

    /** 生产日期 / Production date */
    private LocalDate productionDate;

    /** 有效期至 / Expiry date */
    private LocalDate expiryDate;

    /** 物料条码 / Material barcode */
    private String barcode;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
