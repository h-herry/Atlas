package com.atlas.common.feign;

import com.atlas.common.web.filter.TraceIdFilter;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * Feign 调用 TraceId 透传拦截器 — 自动将当前请求的 traceId 注入 Feign 请求头 /
 * Feign TraceId propagation interceptor — automatically injects current traceId into Feign request headers
 *
 * <p>所有 @FeignClient 调用自动携带 X-Trace-Id，实现跨微服务链路追踪。 /
 * All @FeignClient calls automatically carry X-Trace-Id for cross-microservice tracing.</p>
 *
 * @author Atlas Team
 * @since 1.2.21
 */
@Slf4j
@Configuration
public class TraceIdFeignInterceptor implements RequestInterceptor {

    @Override
    public void apply(RequestTemplate template) {
        String traceId = MDC.get(TraceIdFilter.MDC_TRACE_ID);
        if (StringUtils.hasText(traceId)) {
            template.header(TraceIdFilter.TRACE_ID_HEADER, traceId);
        }
    }

    /**
     * 显式注册为 Bean，确保被 Spring 容器管理 /
     * Explicitly registered as a Bean to ensure Spring container management
     */
    @Bean
    public TraceIdFeignInterceptor traceIdFeignInterceptor() {
        return new TraceIdFeignInterceptor();
    }
}
