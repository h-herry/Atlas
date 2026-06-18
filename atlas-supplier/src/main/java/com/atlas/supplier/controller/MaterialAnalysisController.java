package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.MaterialAnalysis;
import com.atlas.supplier.service.MaterialAnalysisService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物料分析报表 Controller / Material analysis report Controller
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/material/analysis")
@RequiredArgsConstructor
public class MaterialAnalysisController {

    private final MaterialAnalysisService analysisService;

    /** 保存或更新物料分析 / Save or update material analysis */
    @PostMapping
    @RequirePermission("material:analysis:manage")
    public Result<MaterialAnalysis> saveOrUpdate(@RequestBody MaterialAnalysis analysis) {
        return Result.success(analysisService.saveOrUpdate(analysis));
    }

    /** 按物料+周期查询分析 / Query analysis by material + period */
    @GetMapping("/{materialId}/{period}")
    @RequirePermission("material:analysis:view")
    public Result<MaterialAnalysis> getByMaterialAndPeriod(@PathVariable Long materialId,
                                                             @PathVariable String period) {
        return Result.success(analysisService.getByMaterialAndPeriod(materialId, period));
    }

    /** 按周期查询所有物料分析 / List all material analyses by period */
    @GetMapping("/period/{period}")
    @RequirePermission("material:analysis:view")
    public Result<List<MaterialAnalysis>> listByPeriod(@PathVariable String period) {
        return Result.success(analysisService.listByPeriod(period));
    }

    /** 分页查询物料分析 / Paginated query of material analyses */
    @GetMapping("/page")
    @RequirePermission("material:analysis:view")
    public Result<PageResult<MaterialAnalysis>> page(
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) String period,
            @RequestParam(required = false) String costTrend,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MaterialAnalysis> result = analysisService.page(materialId, period, costTrend, page, size);
        return Result.success(PageResult.of(result));
    }
}
