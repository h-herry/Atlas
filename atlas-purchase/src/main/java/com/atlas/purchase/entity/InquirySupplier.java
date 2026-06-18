package com.atlas.purchase.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 询比采购供应商报价实体 / Inquiry-comparison supplier quote entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("inquiry_supplier")
public class InquirySupplier {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联询价单ID / Associated inquiry ID */
    private Long inquiryId;

    /** 供应商ID / Supplier ID */
    private Long supplierId;

    /** 供应商名称 / Supplier name */
    private String supplierName;

    /** 报价金额 / Quoted amount */
    private BigDecimal quoteAmount;

    /** 交货天数 / Delivery days */
    private Integer deliveryDays;

    /** 付款条件 / Payment terms */
    private String paymentTerms;

    /** 备注 / Remarks */
    private String remark;

    /** 报价时间 / Quote time */
    private LocalDateTime quoteTime;
}
