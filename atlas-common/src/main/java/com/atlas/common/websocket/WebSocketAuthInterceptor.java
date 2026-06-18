package com.atlas.common.websocket;

import com.atlas.common.core.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket 消息认证拦截器 — 校验 STOMP 帧中的 JWT Token /
 * WebSocket message authentication interceptor — validates JWT token in STOMP frames
 *
 * <p>对于 CONNECT / SUBSCRIBE / SEND 帧，强制要求 Authorization 头携带有效的 JWT Token；
 * 解析成功后提取 supplier_id（如有）存入 session attributes，
 * 同时将 principal 设置为当前用户，使 {@code @SendToUser} 机制可用。 /
 * For CONNECT / SUBSCRIBE / SEND frames, an Authorization header with a valid JWT token is required;
 * on successful parsing, extracts supplier_id (if present) and stores it in session attributes,
 * and sets principal so the {@code @SendToUser} mechanism works correctly.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;

    private static final String SUPPLIER_ID_ATTR = "supplierId";
    private static final String USER_ID_ATTR = "userId";

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (command == null) {
            return message;
        }

        switch (command) {
            // 连接、订阅、发送消息时均需认证 / Authentication required for CONNECT, SUBSCRIBE, and SEND
            case CONNECT:
            case SUBSCRIBE:
            case SEND:
                authenticate(accessor);
                break;
            case DISCONNECT:
                log.debug("WebSocket DISCONNECT: sessionId={}", accessor.getSessionId());
                break;
            default:
                break;
        }

        return message;
    }

    /**
     * 从 STOMP 帧头中提取并校验 JWT Token /
     * Extract and validate JWT token from STOMP frame headers
     */
    private void authenticate(StompHeaderAccessor accessor) {
        String token = extractToken(accessor);
        if (token == null) {
            log.warn("WebSocket 认证失败 — 缺少 Token: command={}, sessionId={}",
                    accessor.getCommand(), accessor.getSessionId());
            throw new IllegalArgumentException("WebSocket 连接需要有效的 JWT Token / WebSocket connection requires a valid JWT token");
        }

        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            log.warn("WebSocket 认证失败 — Token 无效: command={}, sessionId={}",
                    accessor.getCommand(), accessor.getSessionId());
            throw new IllegalArgumentException("WebSocket Token 无效或已过期 / WebSocket token is invalid or expired");
        }

        Long userId = jwtUtil.getUserId(claims);
        String username = jwtUtil.getUsername(claims);

        // 提取 supplier_id（如果 claims 中存在）/ Extract supplier_id if present in claims
        Object supplierIdObj = claims.get(SUPPLIER_ID_ATTR);
        Long supplierId = supplierIdObj instanceof Number ? ((Number) supplierIdObj).longValue() : null;

        // 存入 session attributes / Store in session attributes
        Map<String, Object> sessionAttributes = accessor.getSessionAttributes();
        if (sessionAttributes != null) {
            sessionAttributes.put(USER_ID_ATTR, userId);
            sessionAttributes.put("username", username);
            if (supplierId != null) {
                sessionAttributes.put(SUPPLIER_ID_ATTR, supplierId);
            }
        }

        // 设置 Principal（使 @SendToUser 可用） / Set Principal (enables @SendToUser)
        Principal principal = new StompPrincipal(userId, username);
        accessor.setUser(principal);

        log.debug("WebSocket 认证成功: userId={}, username={}, supplierId={}, command={}",
                userId, username, supplierId, accessor.getCommand());
    }

    /**
     * 从 STOMP 头中提取 Authorization 头中的 Bearer Token /
     * Extract Bearer token from Authorization header in STOMP headers
     */
    private String extractToken(StompHeaderAccessor accessor) {
        // 优先从 STOMP 原生头获取 / Prefer STOMP native header
        String authorization = accessor.getFirstNativeHeader("Authorization");
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            return authorization.substring(7);
        }

        // 降级: 从 login 参数获取（SockJS 不支持自定义头时的兼容方案） /
        // Fallback: extract from login parameter (compatibility when SockJS doesn't support custom headers)
        String login = accessor.getLogin();
        if (StringUtils.hasText(login)) {
            return login;
        }

        return null;
    }

    /**
     * STOMP Principal 实现 / STOMP Principal implementation
     */
    public record StompPrincipal(Long userId, String username) implements Principal {

        @Override
        public String getName() {
            return username != null ? username : String.valueOf(userId);
        }
    }
}
