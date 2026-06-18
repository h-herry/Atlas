package com.atlas.user.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 登录响应 DTO / Login response DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private String accessToken;
    private Long userId;
    private String username;
    private String realName;
    private Long deptId;
    private String deptName;
    private List<String> roles;
    private List<String> permissions;
}
