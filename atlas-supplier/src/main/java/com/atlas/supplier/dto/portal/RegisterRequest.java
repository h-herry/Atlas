package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 供应商自助注册请求 DTO — 匿名访问，无需登录 /
 * Supplier self-registration request DTO — anonymous access, no login required
 *
 * @author Atlas Team
 * @since 2.2.0
 */
@Data
public class RegisterRequest {

    /** 公司名称 / Company name */
    @NotBlank(message = "公司名称不能为空 / Company name is required")
    private String companyName;

    /** 统一社会信用代码 / Unified social credit code */
    @NotBlank(message = "统一社会信用代码不能为空 / Credit code is required")
    private String creditCode;

    /** 法定代表人 / Legal representative */
    @NotBlank(message = "法定代表人不能为空 / Legal person is required")
    private String legalPerson;

    /** 联系人姓名 / Contact name */
    @NotBlank(message = "联系人不能为空 / Contact name is required")
    private String contactName;

    /** 联系人电话 / Contact phone */
    @NotBlank(message = "联系电话不能为空 / Contact phone is required")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确 / Invalid phone number format")
    private String contactPhone;

    /** 联系人邮箱 / Contact email */
    @NotBlank(message = "邮箱不能为空 / Email is required")
    @Email(message = "邮箱格式不正确 / Invalid email format")
    private String contactEmail;

    /** 行业类别 / Industry category */
    private String industryCategory;

    /** 主营产品 / Main products */
    private String mainProducts;

    /** 年营收（元） / Annual revenue (CNY) */
    private BigDecimal annualRevenue;

    /** 员工人数 / Employee count */
    private Integer employeeCount;

    /** 资质证书列表 / Certificate list */
    private List<CertificateDTO> certificates;
}
