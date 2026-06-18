package com.atlas.purchase.price.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.purchase.price.entity.PriceMaster;
import com.atlas.purchase.price.service.PriceMasterService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 价格主数据 REST API / Price master data REST API
 *
 * <p>管理物料-供应商维度的标准化价格，支持有效期管理和历史趋势查询。 /
 * Manages standardized material-supplier price with validity management and historical trend queries.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/price")
@RequiredArgsConstructor
@Tag(name = "价格主数据 / Price Master Data")
public class PriceMasterController {

    private final PriceMasterService priceMasterService;

    /**
     * 新增价格 / Add new price
     */
    @PostMapping
    @RequirePermission("purchase:add")
    public Result<PriceMaster> add(@RequestBody PriceMaster price) {
        return Result.ok(priceMasterService.add(price));
    }

    /**
     * 更新价格 / Update price
     */
    @PutMapping("/{id}")
    @RequirePermission("purchase:edit")
    public Result<PriceMaster> update(@PathVariable Long id, @RequestBody PriceMaster price) {
        return Result.ok(priceMasterService.update(id, price));
    }

    /**
     * 查当前有效价格 / Query current active price
     */
    @GetMapping("/{materialId}/{supplierId}")
    @RequirePermission("purchase:view")
    public Result<PriceMaster> getActive(@PathVariable Long materialId,
                                          @PathVariable Long supplierId) {
        return priceMasterService.getActivePrice(materialId, supplierId)
                .map(Result::ok)
                .orElse(Result.fail(com.atlas.common.core.enums.ErrorCode.PRICE_NOT_EXIST));
    }

    /**
     * 历史价格趋势 / Historical price trend
     */
    @GetMapping("/{materialId}/history")
    @RequirePermission("purchase:view")
    public Result<List<PriceMaster>> history(@PathVariable Long materialId) {
        return Result.ok(priceMasterService.getHistory(materialId));
    }

    /**
     * 失效价格 / Invalidate price
     */
    @PutMapping("/{id}/invalidate")
    @RequirePermission("purchase:edit")
    public Result<Void> invalidate(@PathVariable Long id) {
        priceMasterService.invalidate(id);
        return Result.ok();
    }
}
