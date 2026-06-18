package com.atlas.supplier.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * ERP 物料编码映射实体 — 对应 material_erp_mapping 表 /
 * ERP material code mapping entity — maps to material_erp_mapping table
 *
 * <p>支持 Atlas 物料编码与多个 ERP 系统（SAP / 金蝶 / 用友 U8）物料编码的双向映射，
 * 按工厂维度隔离，映射异常自动记录待处理队列。 /
 * Supports bidirectional mapping between Atlas material codes and multiple ERP systems (SAP/Kingdee/U8),
 * isolated by plant dimension, auto-queue mapping exceptions for manual resolution.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("material_erp_mapping")
public class MaterialErpMapping implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** Atlas 物料ID / Atlas material ID */
    private Long materialId;

    /** ERP 系统: SAP / KINGDEE / U8 / ERP system identifier */
    private String erpSystem;

    /** ERP 物料编码 / ERP material code */
    private String erpMaterialCode;

    /** 工厂编码 / Plant code */
    private String plantCode;

    /** 映射时间 / Mapped time */
    private LocalDateTime mappedTime;

    /** 状态: 1有效 0无效 / Status: 1=active, 0=inactive */
    private Integer status;

    /** 创建时间 / Created time */
    private LocalDateTime createdAt;

    /** 更新时间 / Updated time */
    private LocalDateTime updatedAt;
}
