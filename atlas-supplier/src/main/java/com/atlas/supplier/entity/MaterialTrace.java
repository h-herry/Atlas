package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 物料全链路追溯实体 / Material full-chain traceability entity
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Data
@TableName("material_trace")
public class MaterialTrace implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 追溯号 / Trace number */
    private String traceNo;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 生产批次号 / Production batch number */
    private String batchNo;

    /** 物料条码 / Material barcode */
    private String barcode;

    /** 追溯类型: RECEIVE入库 / ISSUE出库 / PRODUCE生产 / INSPECT质检 / RETURN退货 / Trace type */
    private String traceType;

    /** 来源单据ID / Source document ID */
    private Long sourceId;

    /** 来源单据号 / Source document number */
    private String sourceNo;

    /** 变动数量 / Quantity change */
    private BigDecimal quantity;

    /** 库位ID / Warehouse location ID */
    private Long warehouseId;

    /** 操作人ID / Operator ID */
    private Long operatorId;

    /** 操作时间 / Operated time */
    private LocalDateTime operatedAt;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;
}
