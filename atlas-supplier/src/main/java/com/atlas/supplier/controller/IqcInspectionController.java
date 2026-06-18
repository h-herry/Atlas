package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.IqcInspection;
import com.atlas.supplier.service.IqcInspectionService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * 来料检验 IQC Controller / Incoming Quality Check (IQC) Controller
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/material/iqc")
@RequiredArgsConstructor
public class IqcInspectionController {

    private final IqcInspectionService inspectionService;

    /** 根据 ID 查询检验单 / Query inspection by ID */
    @GetMapping("/{id}")
    @RequirePermission("material:iqc:view")
    public Result<IqcInspection> getById(@PathVariable Long id) {
        return Result.success(inspectionService.getById(id));
    }

    /** 分页查询检验单 / Paginated query of inspections */
    @GetMapping("/page")
    @RequirePermission("material:iqc:view")
    public Result<PageResult<IqcInspection>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long deliveryId,
            @RequestParam(required = false) String result,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<IqcInspection> p = inspectionService.page(keyword, deliveryId, result, page, size);
        return Result.success(PageResult.of(p));
    }

    /** 执行检验 / Execute inspection */
    @PutMapping("/{id}/inspect")
    @RequirePermission("material:iqc:manage")
    public Result<Void> inspect(@PathVariable Long id,
                                 @RequestParam String result,
                                 @RequestParam BigDecimal qualifiedQty,
                                 @RequestParam BigDecimal defectiveQty,
                                 @RequestParam(required = false) Long inspectorId) {
        inspectionService.inspect(id, result, qualifiedQty, defectiveQty, inspectorId);
        return Result.success();
    }
}
