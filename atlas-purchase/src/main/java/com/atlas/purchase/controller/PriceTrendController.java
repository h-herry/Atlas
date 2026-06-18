package com.atlas.purchase.controller;

import com.atlas.common.core.web.Result;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.purchase.service.PriceTrendService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 历史价格趋势 Controller — REST API for price trend analysis /
 * 历史价格趋势 Controller — 历史价格趋势分析的 REST API 端点
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/inquiry/price")
@RequiredArgsConstructor
@Tag(name = "历史价格趋势 / Price Trend")
public class PriceTrendController {

    private final PriceTrendService priceTrendService;

    /**
     * 按物料+供应商查询月度价格趋势 / Query monthly price trend by material + supplier
     *
     * <p>GET /api/inquiry/price/trend?materialId=XX&supplierId=XX&months=12 /
     * Returns monthly average price, min price, max price, transaction count and trend direction.</p>
     */
    @GetMapping("/trend")
    @Operation(summary = "查询历史价格趋势 / Query historical price trend")
    @ApiOperationSupport(order = 1)
    @RequirePermission("purchase:price:view")
    public Result<List<Map<String, Object>>> trend(
            @RequestParam Long materialId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "12") int months) {
        return Result.success(priceTrendService.queryTrend(materialId, supplierId, months));
    }

    /**
     * 获取物料最新价格 / Get latest price for material
     */
    @GetMapping("/latest/{materialId}")
    @Operation(summary = "获取物料最新价格 / Get latest price for material")
    @ApiOperationSupport(order = 2)
    @RequirePermission("purchase:price:view")
    public Result<Map<String, Object>> latestPrice(@PathVariable Long materialId) {
        return Result.success(priceTrendService.getLatestPrice(materialId));
    }
}
