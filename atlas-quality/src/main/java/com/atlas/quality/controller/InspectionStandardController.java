package com.atlas.quality.controller;

import com.atlas.common.core.web.Result;
import com.atlas.quality.entity.InspectionStandard;
import com.atlas.quality.service.InspectionStandardService;
import com.atlas.common.security.annotation.RequirePermission;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 检验标准与抽样方案控制器 / Inspection standard & sampling plan controller
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/api/quality/inspection-standard")
@RequiredArgsConstructor
@Tag(name = "检验标准管理 / Inspection Standard Management")
public class InspectionStandardController {

    private final InspectionStandardService standardService;

    /**
     * 创建检验标准 / Create inspection standard
     */
    @PostMapping
    @RequirePermission("quality:standard:manage")
    public Result<InspectionStandard> create(@RequestBody InspectionStandard standard) {
        return Result.success(standardService.create(standard));
    }

    /**
     * 更新检验标准 / Update inspection standard
     */
    @PutMapping("/{id}")
    @RequirePermission("quality:standard:manage")
    public Result<Void> update(@PathVariable Long id, @RequestBody InspectionStandard standard) {
        standard.setId(id);
        standardService.update(standard);
        return Result.success();
    }

    /**
     * 启用/停用标准 / Enable or disable standard
     */
    @PutMapping("/{id}/active")
    @RequirePermission("quality:standard:manage")
    public Result<Void> toggleActive(@PathVariable Long id, @RequestParam boolean active) {
        standardService.toggleActive(id, active);
        return Result.success();
    }

    /**
     * 按 ID 查询 / Query by ID
     */
    @GetMapping("/{id}")
    @RequirePermission("quality:standard:view")
    public Result<InspectionStandard> getById(@PathVariable Long id) {
        return Result.success(standardService.getById(id));
    }

    /**
     * 按物料+检验类型查询 / Query by material + inspection type
     */
    @GetMapping("/query")
    @RequirePermission("quality:standard:view")
    public Result<InspectionStandard> getByMaterialAndType(
            @RequestParam Long materialId,
            @RequestParam String inspectType) {
        return Result.success(standardService.getByMaterialAndType(materialId, inspectType));
    }

    /**
     * 分页查询 / Paginated query
     */
    @GetMapping
    @RequirePermission("quality:standard:view")
    public Result<Page<InspectionStandard>> page(
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) String inspectType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(standardService.page(materialId, inspectType, page, size));
    }
}
