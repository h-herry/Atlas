package com.atlas.contract.econtract.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 合同模板DTO — 用于创建/更新模板请求 /
 * Contract template DTO — for create/update template request
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Data
public class TemplateDTO {

    /** 模板名称 / Template name */
    @NotBlank(message = "模板名称不能为空")
    private String templateName;

    /** 模板编码 / Template code */
    @NotBlank(message = "模板编码不能为空")
    private String templateCode;

    /** 模板分类 / Template category */
    @NotBlank(message = "模板分类不能为空")
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
}
