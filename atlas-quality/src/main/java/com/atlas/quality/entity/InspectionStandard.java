package com.atlas.quality.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 检验标准与抽样方案实体 / Inspection standard & sampling plan entity
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
@TableName("inspection_standard")
public class InspectionStandard implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /** 标准编号 / Standard number */
    private String standardNo;

    /** 物料ID / Material ID */
    private Long materialId;

    /** 物料名称 / Material name */
    private String materialName;

    /** 检验类型：IQC/IPQC/OQC / Inspection type */
    private String inspectType;

    /** AQL水平 / AQL level */
    private String aqlLevel;

    /** 抽样方案：GB2828/S-1/S-2/S-3/S-4/100_PERCENT / Sample size type */
    private String sampleSizeType;

    /** 检验水平：I/II/III/S-1/S-2/S-3/S-4 / Inspection level */
    private String inspectionLevel;

    /** 抽样数量 / Sample size */
    private Integer sampleSize;

    /** 合格判定数（Ac） / Acceptance number */
    private Integer acceptLevel;

    /** 不合格判定数（Re） / Rejection number */
    private Integer rejectLevel;

    /** 检验项目清单（JSON） / Inspection item checklist (JSON) */
    private String inspectionItems;

    /** 是否启用 / Whether active */
    private Integer isActive;

    /** 创建人 / Created by */
    private Long createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
