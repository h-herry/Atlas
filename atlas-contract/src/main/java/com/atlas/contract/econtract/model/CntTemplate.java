package com.atlas.contract.econtract.model;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 合同模板实体 — 对应 cnt_template 表 /
 * Contract template entity — corresponds to cnt_template table
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
@TableName("cnt_template")
public class CntTemplate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 模板名称 / Template name */
    private String templateName;

    /** 模板编码 / Template code */
    private String templateCode;

    /** 模板分类: PURCHASE/SERVICE/NDA/FRAMEWORK / Template category */
    private String category;

    /** 模板描述 / Template description */
    private String description;

    /** 模板文件路径 / Template file path */
    private String filePath;

    /** 模板版本号 / Template version */
    private String version;

    /** 是否启用 / Is active */
    private Integer isActive;

    /** 创建人 / Created by */
    private String createdBy;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;
}
