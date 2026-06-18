package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.MaterialTrace;
import com.atlas.supplier.service.MaterialTraceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物料追溯 Controller / Material trace Controller
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/material/trace")
@RequiredArgsConstructor
public class MaterialTraceController {

    private final MaterialTraceService traceService;

    /** 按批次号追溯 / Trace by batch number */
    @GetMapping("/batch/{batchNo}")
    @RequirePermission("material:trace:view")
    public Result<List<MaterialTrace>> traceByBatch(@PathVariable String batchNo) {
        return Result.success(traceService.traceByBatch(batchNo));
    }

    /** 按条码追溯 / Trace by barcode */
    @GetMapping("/barcode/{barcode}")
    @RequirePermission("material:trace:view")
    public Result<List<MaterialTrace>> traceByBarcode(@PathVariable String barcode) {
        return Result.success(traceService.traceByBarcode(barcode));
    }

    /** 分页查询追溯记录 / Paginated query of trace records */
    @GetMapping("/page")
    @RequirePermission("material:trace:view")
    public Result<PageResult<MaterialTrace>> page(
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) String batchNo,
            @RequestParam(required = false) String traceType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<MaterialTrace> result = traceService.page(materialId, batchNo, traceType, page, size);
        return Result.success(PageResult.of(result));
    }
}
