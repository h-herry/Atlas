package com.atlas.purchase.feign;

import com.atlas.common.web.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 库存服务 Feign 降级实现 — 触发熔断时返回兜底结果 / Inventory service Feign fallback — returns fallback result when circuit breaker triggers
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Component
public class InventoryFeignFallback implements InventoryFeignClient {

    @Override
    public Result<java.util.Map<String, Object>> deduct(List<InventoryDeductRequest> items) {
        log.warn("[Sentinel熔断] atlas-inventory 库存扣减服务不可用，items={}", items.size());
        return Result.fail(12020, "库存服务暂不可用，已触发熔断降级，请稍后重试");
    }
}
