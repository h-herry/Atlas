package com.atlas.purchase.feign;

import com.atlas.common.web.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Map;

/**
 * 库存服务 Feign 客户端（带 Sentinel 熔断降级） / Inventory service Feign client (with Sentinel circuit breaker)
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@FeignClient(name = "atlas-inventory", fallback = InventoryFeignFallback.class)
public interface InventoryFeignClient {

    /**
     * 批量扣减库存 / Batch inventory deduction
     */
    @PostMapping("/api/inventory/deduct")
    Result<Map<String, Object>> deduct(@RequestBody List<InventoryDeductRequest> items);

    /**
     * 库存扣减请求 / Inventory deduction request
     */
    @lombok.Data
    class InventoryDeductRequest {
        private Long skuId;
        private java.math.BigDecimal qty;
        private String orderNo;
    }
}
