package com.atlas.supplier.dto.portal;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 供应商登录请求 DTO / Supplier login request DTO
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Data
public class SupplierLoginRequest {

    /** 登录账号（手机号或用户名） / Login account (phone number or username) */
    @NotBlank(message = "账号不能为空 / Account is required")
    private String account;

    /** 登录密码 / Login password */
    @NotBlank(message = "密码不能为空 / Password is required")
    private String password;

    /** 验证码（可选，安全策略开启时必填） / Verification code (optional, required when security policy is on) */
    private String captcha;
}
