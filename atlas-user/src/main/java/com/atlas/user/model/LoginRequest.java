package com.atlas.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 登录请求 DTO / Login request DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @jakarta.validation.constraints.NotBlank(message = "用户名不能为空")
    private String username;

    @jakarta.validation.constraints.NotBlank(message = "密码不能为空")
    private String password;
}
