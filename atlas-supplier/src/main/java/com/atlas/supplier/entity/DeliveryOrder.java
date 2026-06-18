package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 发货单实体 — 对应 delivery_order 表 / Delivery order entity — maps to delivery_order table
 *
 * @author atlas
 */
@Data
@TableName("delivery_order")
public class DeliveryOrder {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 发货单号 / Delivery order number */
    private String deliveryNo;

    /** 关联采购单ID / Purchase order ID */
    private Long purchaseOrderId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 物流公司 / Logistics company */
    private String logisticsCompany;

    /** 物流单号 / Tracking number */
    private String trackingNo;

    /** 预计到达日期 / Estimated arrival date */
    private LocalDate estimatedArriveDate;

    /** 实际到达日期 / Actual arrival date */
    private LocalDate actualArriveDate;

    /** 发货明细（JSON: [{materialId, qty}]） / Delivery items (JSON) */
    private String deliveryItems;

    /** 状态: 0待发货 1运输中 2已到货 3已签收 4部分退货 / Status: 0=pending, 1=in transit, 2=arrived, 3=signed, 4=partial return */
    private Integer status;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
