package com.atlas.user.controller;

import com.atlas.common.annotation.AuditLog;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.common.core.util.JwtUtil;
import com.atlas.common.web.Result;
import com.atlas.user.model.LoginRequest;
import com.atlas.user.model.LoginResponse;
import com.atlas.user.service.LoginService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器 — 登录 / 登出 / Token 刷新 /
 * Authentication controller — login / logout / token refresh
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final LoginService loginService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    @AuditLog(module = "用户管理", action = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse resp = loginService.login(request);
        return Result.ok(resp);
    }

    @PostMapping("/refresh")
    public Result<String> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);
        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            throw new BizException(ErrorCode.TOKEN_EXPIRED);
        }
        Long userId = jwtUtil.getUserId(claims);
        String username = jwtUtil.getUsername(claims);
        String newToken = jwtUtil.createToken(userId, username,
                Map.of("deptId", claims.get("deptId"), "realName", claims.get("realName")));
        return Result.ok(newToken);
    }
}
