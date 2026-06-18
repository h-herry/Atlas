package com.atlas.supplier.config;

import com.atlas.common.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 供应商端（Portal）安全配置 — 独立 JWT 过滤器，拦截 /portal/** 路径 /
 * Supplier portal security configuration — independent JWT filter, intercepts /portal/** paths
 *
 * <p>与企业端 JWT（role=ADMIN/USER）隔离，供应商端仅允许 role=SUPPLIER。 /
 * Isolated from enterprise JWT (role=ADMIN/USER), portal only allows role=SUPPLIER.</p>
 *
 * <p>供应商只能操作属于自己的数据（supplier_id 匹配）。 /
 * Suppliers can only operate on their own data (supplier_id matching).</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SupplierSecurityConfig {

    private final JwtUtil jwtUtil;

    /**
     * 供应商端安全过滤链 — 仅拦截 /portal/**，使用独立 JWT 认证 /
     * Supplier portal security filter chain — only intercepts /portal/**, uses independent JWT auth
     */
    @Bean
    @Order(1)
    public SecurityFilterChain supplierPortalFilterChain(HttpSecurity http) throws Exception {
        http.securityMatcher("/portal/**")
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // 登录和注册接口匿名访问 / Login and registration endpoints allow anonymous access
                .requestMatchers("/portal/auth/login").permitAll()
                .requestMatchers("/portal/auth/register").permitAll()
                .requestMatchers("/portal/auth/register/**").permitAll()
                // 其余 portal 路径需要 SUPPLIER 角色 / All other portal paths require SUPPLIER role
                .anyRequest().authenticated()
            )
            .addFilterBefore(supplierJwtFilter(), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 供应商 JWT 认证过滤器 / Supplier JWT authentication filter
     */
    @Bean
    public OncePerRequestFilter supplierJwtFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {

                // 跳过登录和注册接口 / Skip login and registration endpoints
                String uri = request.getRequestURI();
                if (uri.contains("/portal/auth/login") || uri.contains("/portal/auth/register")) {
                    filterChain.doFilter(request, response);
                    return;
                }

                String token = extractToken(request);
                if (!StringUtils.hasText(token)) {
                    respondUnauthorized(response, "缺少认证令牌 / Missing authentication token");
                    return;
                }

                try {
                    Claims claims = jwtUtil.parseToken(token);

                    // 必须包含 role=SUPPLIER / Must contain role=SUPPLIER
                    String role = claims.get("role", String.class);
                    if (!"SUPPLIER".equals(role)) {
                        respondUnauthorized(response, "仅限供应商角色访问 / Supplier role required");
                        return;
                    }

                    // 提取 supplier_id 放入 request attribute / Extract supplier_id into request attribute
                    Long supplierId = claims.get("supplier_id", Long.class);
                    if (supplierId == null) {
                        respondUnauthorized(response, "令牌缺少 supplier_id / Token missing supplier_id");
                        return;
                    }

                    // 设置安全上下文 / Set security context
                    request.setAttribute("currentSupplierId", supplierId);
                    request.setAttribute("currentSupplierRole", role);

                    log.debug("供应商认证成功: supplierId={}, path={}", supplierId, request.getRequestURI());
                    filterChain.doFilter(request, response);

                } catch (JwtException e) {
                    log.warn("供应商JWT校验失败: {}", e.getMessage());
                    respondUnauthorized(response, "令牌无效或已过期 / Invalid or expired token");
                }
            }
        };
    }

    /**
     * 从请求头提取 Bearer Token / Extract Bearer token from request header
     */
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }

    /**
     * 返回 401 未授权响应 / Return 401 unauthorized response
     */
    private void respondUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":401,\"message\":\"" + message + "\"}");
    }

    // ==================== 辅助方法 / Helper Methods ====================

    /**
     * 从当前请求获取供应商ID — 供 Service 层调用 /
     * Get supplier ID from current request — for Service layer usage
     */
    public static Long getCurrentSupplierId() {
        var request = ((org.springframework.web.context.request.ServletRequestAttributes)
                org.springframework.web.context.request.RequestContextHolder.getRequestAttributes()).getRequest();
        return (Long) request.getAttribute("currentSupplierId");
    }
}
