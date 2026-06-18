package com.atlas.common.config;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.callback.BlockExceptionHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.web.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;

import java.io.PrintWriter;

/**
 * Sentinel 通用配置 — 统一限流/熔断降级返回友好 JSON /
 * Sentinel common config — unified rate limiting / circuit breaking returning friendly JSON
 *
 * <p>与 {@link com.atlas.common.annotation.RateLimit @RateLimit} 本地限流形成互补：
 * <br>@RateLimit 用于细粒度方法级本地限流，Sentinel 用于分布式流量控制。 /
 * Complements @RateLimit local rate limiting: @RateLimit for fine-grained method-level local limiting,
 * Sentinel for distributed traffic control.</p>
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Configuration
public class SentinelConfig implements BlockExceptionHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       BlockException exception) throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(429);

        Result<?> result;
        if (exception instanceof FlowException) {
            result = Result.fail(ErrorCode.RATE_LIMIT_EXCEEDED);
            log.warn("Sentinel 限流触发: uri={}", request.getRequestURI());
        } else if (exception instanceof DegradeException) {
            result = Result.fail(ErrorCode.CIRCUIT_BREAKER_OPEN);
            log.warn("Sentinel 熔断触发: uri={}", request.getRequestURI());
        } else {
            result = Result.fail(ErrorCode.INTERNAL_ERROR);
            log.error("Sentinel 未知阻断: uri={} type={}",
                    request.getRequestURI(), exception.getClass().getSimpleName());
        }

        PrintWriter writer = response.getWriter();
        writer.write(objectMapper.writeValueAsString(result));
        writer.flush();
    }
}
