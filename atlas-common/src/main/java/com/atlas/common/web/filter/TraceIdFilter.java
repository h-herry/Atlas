package com.atlas.common.web.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * TraceId 过滤器 — 为每个 HTTP 请求生成全局 traceId 并放入 MDC /
 * TraceId filter — generates a global traceId for each HTTP request and puts it into MDC
 *
 * <p>优先级最高（Ordered.HIGHEST_PRECEDENCE），确保 traceId 覆盖整个请求链路。 /
 * Highest priority (Ordered.HIGHEST_PRECEDENCE) to ensure traceId covers the entire request chain.</p>
 *
 * <p>traceId 来源优先级 / traceId source priority:</p>
 * <ol>
 *   <li>请求头 X-Trace-Id（跨服务透传）/ Request header X-Trace-Id (cross-service propagation)</li>
 *   <li>自动生成 UUID 36 位简写 / Auto-generated UUID 36-char shorthand</li>
 * </ol>
 *
 * @author Atlas Team
 * @since 1.2.21
 */
@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdFilter extends OncePerRequestFilter {

    /** TraceId 请求头名称 / TraceId request header name */
    public static final String TRACE_ID_HEADER = "X-Trace-Id";

    /** MDC 中的 traceId 键 / traceId key in MDC */
    public static final String MDC_TRACE_ID = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            // 优先从请求头获取 traceId / Prefer traceId from request header
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (!StringUtils.hasText(traceId)) {
                // 自动生成 / Auto-generate
                traceId = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
            }
            MDC.put(MDC_TRACE_ID, traceId);
            // 将 traceId 写入响应头，方便前端排查 / Write traceId to response header for frontend troubleshooting
            response.setHeader(TRACE_ID_HEADER, traceId);
            filterChain.doFilter(request, response);
        } finally {
            // 请求结束后清理 MDC，避免内存泄漏 / Clean MDC after request to avoid memory leak
            MDC.remove(MDC_TRACE_ID);
        }
    }
}
