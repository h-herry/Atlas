package com.atlas.supplier.controller;

import com.atlas.common.core.web.Result;
import com.atlas.supplier.entity.MaterialErpMapping;
import com.atlas.supplier.service.MaterialErpMappingService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ERP 物料编码映射 Controller — REST API for ERP material code mapping /
 * ERP 物料编码映射 Controller — ERP 物料编码映射管理的 REST API 端点
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/material/erp-mapping")
@RequiredArgsConstructor
@Tag(name = "ERP 物料编码映射 / ERP Material Code Mapping")
public class MaterialErpMappingController {

    private final MaterialErpMappingService materialErpMappingService;

    /**
     * 按物料ID 查询所有 ERP 映射 / Query all ERP mappings by material ID
     */
    @GetMapping("/material/{materialId}")
    @Operation(summary = "按物料ID查询 ERP 映射 / Query ERP mappings by material ID")
    @ApiOperationSupport(order = 1)
    public Result<List<MaterialErpMapping>> listByMaterialId(@PathVariable Long materialId) {
        return Result.success(materialErpMappingService.findByMaterialId(materialId));
    }

    /**
     * 按 ERP 系统查询映射 / Query mappings by ERP system
     */
    @GetMapping("/erp-system/{erpSystem}")
    @Operation(summary = "按 ERP 系统查询映射 / Query mappings by ERP system")
    @ApiOperationSupport(order = 2)
    public Result<List<MaterialErpMapping>> listByErpSystem(@PathVariable String erpSystem) {
        return Result.success(materialErpMappingService.listByErpSystem(erpSystem));
    }

    /**
     * 按 ERP 系统+编码反查 / Reverse lookup by ERP system + code
     */
    @GetMapping("/lookup")
    @Operation(summary = "按 ERP 系统+编码反查 Atlas 物料 / Reverse lookup by ERP system + code")
    @ApiOperationSupport(order = 3)
    public Result<MaterialErpMapping> lookup(@RequestParam String erpSystem,
                                              @RequestParam String erpMaterialCode) {
        return Result.success(materialErpMappingService.findByErpCode(erpSystem, erpMaterialCode));
    }

    /**
     * 新增映射 / Create mapping
     */
    @PostMapping
    @Operation(summary = "新增 ERP 编码映射 / Create ERP code mapping")
    @ApiOperationSupport(order = 4)
    public Result<MaterialErpMapping> create(@RequestBody MaterialErpMapping mapping) {
        return Result.success(materialErpMappingService.create(mapping));
    }

    /**
     * 解绑映射 / Unbind mapping
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "解绑 ERP 映射 / Unbind ERP mapping")
    @ApiOperationSupport(order = 5)
    public Result<Void> unbind(@PathVariable Long id) {
        materialErpMappingService.unbind(id);
        return Result.success();
    }

    /**
     * 批量解绑 / Batch unbind
     */
    @DeleteMapping("/batch")
    @Operation(summary = "批量解绑 ERP 映射 / Batch unbind ERP mappings")
    @ApiOperationSupport(order = 6)
    public Result<Void> unbindBatch(@RequestBody List<Long> ids) {
        materialErpMappingService.unbindBatch(ids);
        return Result.success();
    }
}
