package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 供应商发货单实体 / Supplier delivery order entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("supplier_delivery")
public class SupplierDelivery implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联采购订单ID / Purchase order ID */
    private Long purchaseOrderId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 发货单号 / Delivery number */
    private String deliveryNo;

    /** 状态: 0待发货 1已发货 2部分收货 3已收货 / Status: 0=pending, 1=shipped, 2=partial received, 3=received */
    private Integer status;

    /** 物流单号 / Tracking number */
    private String trackingNo;

    /** 承运商 / Carrier */
    private String carrier;

    /** 预计到货日期 / Estimated arrival */
    private LocalDate estimatedArrival;

    /** 发货时间 / Shipped time */
    private LocalDateTime shippedAt;

    /** 条码标签URL / Barcode label URL */
    private String barcodeLabelUrl;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
