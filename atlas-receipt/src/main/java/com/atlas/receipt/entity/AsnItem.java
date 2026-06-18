package com.atlas.receipt.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * ASN 明细实体 / ASN item entity
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("asn_item")
public class AsnItem implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联ASN ID / Associated ASN ID */
    private Long asnId;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 物料名称 / Material name */
    private String materialName;

    /** 发货数量 / Shipped quantity */
    private BigDecimal quantity;

    /** 单位 / Unit */
    private String unit;

    /** 生产批次号 / Production batch number */
    private String batchNo;

    /** 包装方式 / Packaging type */
    private String packagingType;

    /** 件数 / Package count */
    private Integer packageCount;

    /** 毛重(kg) / Gross weight */
    private BigDecimal grossWeight;

    /** 净重(kg) / Net weight */
    private BigDecimal netWeight;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
