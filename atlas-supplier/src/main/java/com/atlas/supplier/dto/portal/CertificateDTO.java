package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.LocalDate;

/**
 * 资质证书 DTO — 入驻申请中的资质信息 /
 * Certificate DTO — qualification info within onboarding application
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
public class CertificateDTO {

    /** 证书类型 / Certificate type */
    @NotBlank(message = "证书类型不能为空 / Certificate type is required")
    private String certType;

    /** 证书名称 / Certificate name */
    @NotBlank(message = "证书名称不能为空 / Certificate name is required")
    private String certName;

    /** 证书编号 / Certificate number */
    private String certNumber;

    /** 证书文件路径 / Certificate file path */
    private String filePath;

    /** 签发日期 / Issue date */
    private LocalDate issueDate;

    /** 到期日期 / Expire date */
    private LocalDate expireDate;
}
