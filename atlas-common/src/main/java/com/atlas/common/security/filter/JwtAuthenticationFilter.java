package com.atlas.common.security.filter;

import com.atlas.common.core.exception.UnauthorizedException;
import com.atlas.common.core.util.JwtUtil;
import com.atlas.common.security.service.RenewTokenHolder;
import com.atlas.common.security.service.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT 认证过滤器 — 每个请求拦截提取 Token 并设置 SecurityContext /
 * JWT authentication filter — intercepts each request, extracts token and sets SecurityContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenService tokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Claims claims = jwtUtil.parseToken(token);
        if (claims == null) {
            filterChain.doFilter(request, response);
            return;
        }

        Long userId = jwtUtil.getUserId(claims);
        String username = jwtUtil.getUsername(claims);

        // 从 Redis 获取用户角色权限 / Get user role permissions from Redis
        List<String> permissions = tokenService.getPermissions(userId);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList())
                );
        auth.setDetails(claims); // 把 claims 作为 details 传递（含 deptId 等信息） / Pass claims as details (contains deptId etc.)
        SecurityContextHolder.getContext().setAuthentication(auth);

        // 续期：Token 距离过期不到一半时间时刷新 / Renewal: refresh if token is less than half remaining
        tokenService.renewIfNeeded(token, claims);

        filterChain.doFilter(request, response);

        // 若 TokenService 生成新 Token，写入响应头 / If TokenService generated new token, write to response header
        String newToken = RenewTokenHolder.get();
        if (newToken != null) {
            response.setHeader("X-New-Token", newToken);
            RenewTokenHolder.clear();
        }
    }

    private String extractToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}
