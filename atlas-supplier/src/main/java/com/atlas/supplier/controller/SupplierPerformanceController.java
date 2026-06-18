package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.SupplierPerformance;
import com.atlas.supplier.service.SupplierPerformanceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 供应商绩效 Controller / Supplier performance Controller
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/material/performance")
@RequiredArgsConstructor
@Tag(name = "供应商绩效 / Supplier Performance")
public class SupplierPerformanceController {

    private final SupplierPerformanceService performanceService;

    /** 计算并保存绩效 / Calculate and save performance */
    @PostMapping
    @RequirePermission("material:performance:manage")
    public Result<SupplierPerformance> calculate(@RequestBody SupplierPerformance performance) {
        return Result.success(performanceService.calculate(performance));
    }

    /** 按供应商+周期查询绩效 / Query performance by supplier + period */
    @GetMapping("/{supplierId}/{period}")
    @RequirePermission("material:performance:view")
    public Result<SupplierPerformance> getBySupplierAndPeriod(@PathVariable Long supplierId,
                                                                @PathVariable String period) {
        return Result.success(performanceService.getBySupplierAndPeriod(supplierId, period));
    }

    /** 按周期查询所有供应商绩效 / List all supplier performances by period */
    @GetMapping("/period/{period}")
    @RequirePermission("material:performance:view")
    public Result<List<SupplierPerformance>> listByPeriod(@PathVariable String period) {
        return Result.success(performanceService.listByPeriod(period));
    }

    /** 分页查询绩效 / Paginated query of performances */
    @GetMapping("/page")
    @RequirePermission("material:performance:view")
    public Result<PageResult<SupplierPerformance>> page(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String grade,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SupplierPerformance> result = performanceService.page(supplierId, period, grade, page, size);
        return Result.success(PageResult.of(result));
    }
}
