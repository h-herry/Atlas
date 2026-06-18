package com.atlas.user.auth.controller;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.web.Result;
import com.atlas.user.auth.service.AuthService;
import io.jsonwebtoken.Claims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Token 校验控制器 — 供网关 Feign 内部调用 /
 * Token verification controller — for internal Feign calls from gateway
 * <p>
 * 网关层将请求头中的 Token 转发给本接口以校验合法性，无需携带用户上下文。
 * Gateway forwards the token from request header to this endpoint for validation, no user context needed.
 */
@RestController
@RequestMapping("/token")
@RequiredArgsConstructor
@Tag(name = "Token 校验 / Token Verification")
public class TokenVerifyController {

    private final AuthService authService;

    /**
     * 校验 Token 有效性（内部 Feign 调用） /
     * Validate token (internal Feign call)
     *
     * <pre>
     * GET /token/validate?token=xxx
     * Response:
     *   有效 / valid:   { "code": 200, "data": { "valid": true, "userId": ..., "username": ..., "channel": ... } }
     *   无效 / invalid: { "code": 200, "data": { "valid": false } }
     * </pre>
     */
    @GetMapping("/validate")
    @Operation(summary = "Token 校验（内部 Feign） / Token validation (internal Feign)")
    public Result<Map<String, Object>> validate(@RequestParam("token") String token) {
        if (token == null || token.isBlank()) {
            return Result.fail(ErrorCode.TOKEN_INVALID);
        }
        Claims claims = authService.checkToken(token);
        if (claims == null) {
            return Result.ok(Map.of("valid", false));
        }
        return Result.ok(Map.of(
                "valid", true,
                "userId", claims.getSubject(),
                "username", claims.get("username"),
                "channel", claims.get("channel"),
                "deptId", claims.get("deptId"),
                "roles", claims.get("roles"),
                "permissions", claims.get("permissions")
        ));
    }
}
