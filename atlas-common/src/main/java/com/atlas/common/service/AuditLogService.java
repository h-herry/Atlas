package com.atlas.common.service;

import com.atlas.common.core.util.JwtUtil;
import com.atlas.common.entity.AuditLog;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 审计日志服务 — 异步写入，不影响主业务流程 /
 * Audit log service — async write, does not block main business flow
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final JwtUtil jwtUtil;

    /**
     * 异步写入审计日志。
     * <p>注意：当前版本审计日志仅通过日志输出，后续可接入数据库或 ELK。 /
     * Async write audit log.
     * <p>Note: current version only outputs via log; can be upgraded to DB or ELK.</p>
     */
    @Async
    public void record(AuditLog auditLog) {
        try {
            auditLog.setCreatedAt(LocalDateTime.now());
            String json = OBJECT_MAPPER.writeValueAsString(auditLog);
            log.info("[AUDIT] {}", json);
        } catch (JsonProcessingException e) {
            log.error("[AUDIT] 审计日志序列化失败", e);
        }
    }

    /**
     * 从当前请求上下文提取用户/URL/IP 信息，构建 AuditLog 对象 /
     * Extract user/URL/IP info from current request context, build AuditLog object
     */
    public AuditLog build(String module, String operation, String description,
                           Object[] args, long durationMs, String responseStatus) {
        AuditLog auditLog = new AuditLog();
        auditLog.setModule(module);
        auditLog.setOperation(operation);
        auditLog.setDescription(description);
        auditLog.setDurationMs(durationMs);
        auditLog.setResponseStatus(responseStatus);

        // 序列化请求参数（截断，最大 2000 字符） / Serialize request params (truncated, max 2000 chars)
        try {
            String paramsJson = OBJECT_MAPPER.writeValueAsString(args);
            if (paramsJson.length() > 2000) {
                paramsJson = paramsJson.substring(0, 2000) + "...";
            }
            auditLog.setRequestParams(paramsJson);
        } catch (JsonProcessingException ignored) {
            auditLog.setRequestParams("[serialize error]");
        }

        // 从请求上下文提取信息 / Extract info from request context
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            auditLog.setRequestUri(request.getRequestURI());
            auditLog.setRequestMethod(request.getMethod());
            auditLog.setIpAddress(getClientIp(request));

            // 从 JWT 提取用户信息 / Extract user info from JWT
            extractUserInfo(request, auditLog);
        }

        return auditLog;
    }

    private void extractUserInfo(HttpServletRequest request, AuditLog auditLog) {
        String authHeader = request.getHeader("Authorization");
        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
            return;
        }
        try {
            String token = authHeader.substring(7);
            Claims claims = jwtUtil.parseToken(token);
            auditLog.setUserId(jwtUtil.getUserId(claims));
            Optional.ofNullable(claims.get("realName"))
                    .ifPresent(name -> auditLog.setUsername(name.toString()));
        } catch (Exception e) {
            log.debug("审计日志提取用户信息失败: {}", e.getMessage());
        }
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (!StringUtils.hasText(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}
