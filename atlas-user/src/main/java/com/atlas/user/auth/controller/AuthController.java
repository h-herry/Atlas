package com.atlas.user.auth.controller;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.common.web.Result;
import com.atlas.user.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 认证控制器 — 双通道登录/登出/Token 刷新/校验 /
 * Authentication controller — dual-channel login/logout/token refresh/validate
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "认证管理 / Authentication")
public class AuthController {

    private final AuthService authService;

    // ==================== 登录 / Login ====================

    /**
     * 双通道登录 /
     * Dual-channel login
     *
     * <pre>
     * POST /auth/login
     * Body: { "username": "...", "password": "...", "channel": "enterprise|supplier" }
     * Response: { "code": 200, "data": { "accessToken": "...", "userId": ..., ... } }
     * </pre>
     */
    @PostMapping("/login")
    @Operation(summary = "双通道登录 / Dual-channel login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body,
                                              HttpServletRequest request) {
        String username = body.get("username");
        String password = body.get("password");
        String channel = body.getOrDefault("channel", "enterprise");

        if (username == null || username.isBlank()) {
            return Result.fail(ErrorCode.BAD_REQUEST.getCode(), "用户名不能为空");
        }
        if (password == null || password.isBlank()) {
            return Result.fail(ErrorCode.BAD_REQUEST.getCode(), "密码不能为空");
        }
        if (!"enterprise".equals(channel) && !"supplier".equals(channel)) {
            return Result.fail(ErrorCode.BAD_REQUEST.getCode(), "通道参数无效，可选值: enterprise, supplier");
        }

        String ip = getClientIp(request);
        Map<String, Object> result = authService.login(username, password, channel, ip);
        return Result.ok(result);
    }

    // ==================== 登出 / Logout ====================

    /**
     * 登出：Token 加入黑名单 /
     * Logout: add token to blacklist
     */
    @PostMapping("/logout")
    @Operation(summary = "登出 / Logout")
    public Result<Void> logout(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        String token = authHeader.substring(7);
        authService.logout(token);
        return Result.ok();
    }

    // ==================== Token 刷新 / Token Refresh ====================

    /**
     * 刷新 Token /
     * Refresh token
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新 Token / Refresh token")
    public Result<Map<String, String>> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new BizException(ErrorCode.UNAUTHORIZED);
        }
        String oldToken = authHeader.substring(7);
        String newToken = authService.refresh(oldToken);
        return Result.ok(Map.of("accessToken", newToken));
    }

    // ==================== Token 校验 / Token Check ====================

    /**
     * 校验当前 Token 是否有效 /
     * Check if current token is valid
     */
    @GetMapping("/check")
    @Operation(summary = "Token 有效性校验 / Token validity check")
    public Result<Map<String, Object>> check(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.ok(Map.of("valid", false, "reason", "缺少 Authorization 头"));
        }
        String token = authHeader.substring(7);
        Claims claims = authService.checkToken(token);
        if (claims == null) {
            return Result.ok(Map.of("valid", false, "reason", "Token 无效或已过期"));
        }
        return Result.ok(Map.of(
                "valid", true,
                "userId", claims.getSubject(),
                "username", claims.get("username"),
                "channel", claims.get("channel"),
                "expiresAt", claims.getExpiration().getTime() / 1000
        ));
    }

    // ==================== 工具方法 / Utility ====================

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个 / For multi-proxy, take first IP
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
