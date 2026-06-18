package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.ProductionProgress;
import com.atlas.supplier.service.ProductionProgressService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 生产进度 Controller / Production progress Controller
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/material/progress")
@RequiredArgsConstructor
public class ProductionProgressController {

    private final ProductionProgressService progressService;

    /** 填报生产进度 / Report production progress */
    @PostMapping
    @RequirePermission("material:progress:manage")
    public Result<ProductionProgress> report(@RequestBody ProductionProgress progress) {
        return Result.success(progressService.report(progress));
    }

    /** 更新生产进度 / Update production progress */
    @PutMapping
    @RequirePermission("material:progress:manage")
    public Result<ProductionProgress> update(@RequestBody ProductionProgress progress) {
        return Result.success(progressService.update(progress));
    }

    /** 按订单查询生产进度 / Query progress by order */
    @GetMapping("/order/{orderId}")
    @RequirePermission("material:progress:view")
    public Result<ProductionProgress> getByOrder(@PathVariable Long orderId,
                                                   @RequestParam(required = false) Long materialId) {
        return Result.success(progressService.getByOrder(orderId, materialId));
    }

    /** 分页查询生产进度 / Paginated query of production progress */
    @GetMapping("/page")
    @RequirePermission("material:progress:view")
    public Result<PageResult<ProductionProgress>> page(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<ProductionProgress> result = progressService.page(orderId, supplierId, page, size);
        return Result.success(PageResult.of(result));
    }
}
