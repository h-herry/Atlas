package com.atlas.purchase.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.purchase.entity.SingleSourcePurchase;
import com.atlas.purchase.service.SingleSourceService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 单一来源采购 REST API / Single-source procurement REST API
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/purchase/single-source")
@RequiredArgsConstructor
@Tag(name = "单一来源采购 / Single Source Procurement")
public class SingleSourceController {

    private final SingleSourceService singleSourceService;

    /**
     * 分页查询单一来源采购 / Paginated query of single-source procurements
     */
    @GetMapping("/page")
    @RequirePermission("purchase:bidding:view")
    public Result<PageResult<SingleSourcePurchase>> page(
            @RequestParam(defaultValue = "1") int current,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        IPage<SingleSourcePurchase> page = singleSourceService.page(new Page<>(current, size), keyword, status);
        return Result.success(PageResult.of(page));
    }

    /**
     * 查询单一来源采购详情 / Query single-source procurement detail
     */
    @GetMapping("/{id}")
    @RequirePermission("purchase:bidding:view")
    public Result<SingleSourcePurchase> getById(@PathVariable Long id) {
        return Result.success(singleSourceService.getById(id));
    }

    /**
     * 开始谈判 / Start negotiation
     */
    @PutMapping("/{id}/negotiate")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> startNegotiation(@PathVariable Long id,
                                          @RequestParam String supplierName,
                                          @RequestParam String singleSourceReason,
                                          @RequestParam BigDecimal negotiationAmount) {
        singleSourceService.startNegotiation(id, supplierName, singleSourceReason, negotiationAmount);
        return Result.success();
    }

    /**
     * 完成谈判成交 / Complete negotiation and transact
     */
    @PutMapping("/{id}/complete")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> complete(@PathVariable Long id,
                                  @RequestParam BigDecimal finalAmount,
                                  @RequestParam String negotiatedBy) {
        singleSourceService.complete(id, finalAmount, negotiatedBy);
        return Result.success();
    }

    /**
     * 终止采购 / Terminate procurement
     */
    @PutMapping("/{id}/terminate")
    @RequirePermission("purchase:bidding:manage")
    public Result<Void> terminate(@PathVariable Long id) {
        singleSourceService.terminate(id);
        return Result.success();
    }
}
