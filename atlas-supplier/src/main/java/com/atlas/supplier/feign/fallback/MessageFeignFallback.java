package com.atlas.supplier.feign.fallback;

import com.atlas.common.web.Result;
import com.atlas.supplier.feign.MessageFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * MessageFeignClient 降级处理 — 当 atlas-message 不可用时优雅降级 /
 * MessageFeignClient fallback — graceful degradation when atlas-message is unavailable
 *
 * <p>降级策略: 记录错误日志，返回失败结果，不影响主业务流程 /
 * Fallback strategy: log errors, return failed result, do not block main business flow</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Component
public class MessageFeignFallback implements MessageFeignClient {

    @Override
    public Result<Map<String, Object>> pushToSupplier(Map<String, Object> request) {
        log.warn("MessageFeign 降级: pushToSupplier 调用失败, request={}", request);
        return Result.fail("消息推送服务暂不可用 / Message push service temporarily unavailable");
    }

    @Override
    public Result<Map<String, Object>> broadcastToAll(Map<String, Object> request) {
        log.warn("MessageFeign 降级: broadcastToAll 调用失败, request={}", request);
        return Result.fail("消息广播服务暂不可用 / Message broadcast service temporarily unavailable");
    }

    @Override
    public Result<Map<String, Object>> pushToUser(Map<String, Object> request) {
        log.warn("MessageFeign 降级: pushToUser 调用失败, request={}", request);
        return Result.fail("用户消息推送服务暂不可用 / User message push service temporarily unavailable");
    }
}
