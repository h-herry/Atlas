package com.atlas.supplier.controller;

import com.atlas.common.core.web.Result;
import com.atlas.supplier.entity.SupplierClassification;
import com.atlas.supplier.service.SupplierClassificationService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 供应商分级 Controller — REST API for supplier classification management /
 * 供应商分级 Controller — 供应商分级管理的 REST API 端点
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/supplier/classification")
@RequiredArgsConstructor
@Tag(name = "供应商分级 / Supplier Classification")
public class SupplierClassificationController {

    private final SupplierClassificationService classificationService;

    /**
     * 供应商定级 / Classify supplier
     */
    @PostMapping
    @Operation(summary = "供应商定级 / Classify supplier")
    @ApiOperationSupport(order = 1)
    public Result<SupplierClassification> classify(@RequestBody SupplierClassification classification) {
        return Result.success(classificationService.classify(classification));
    }

    /**
     * 升降级 / Promotion or demotion
     */
    @PutMapping("/promote-demote")
    @Operation(summary = "供应商升降级 / Promotion or demotion")
    @ApiOperationSupport(order = 2)
    public Result<SupplierClassification> promoteOrDemote(@RequestBody Map<String, Object> body) {
        return Result.success(classificationService.promoteOrDemote(
            Long.valueOf(body.get("supplierId").toString()),
            (String) body.get("newGrade"),
            (String) body.get("reason"),
            Long.valueOf(body.get("assessorId").toString()),
            (String) body.get("assessorName")));
    }

    /**
     * 查询当前有效分级 / Query current active classification
     */
    @GetMapping("/{supplierId}/current")
    @Operation(summary = "查询供应商当前分级 / Query current classification")
    @ApiOperationSupport(order = 3)
    public Result<SupplierClassification> current(@PathVariable Long supplierId) {
        return Result.success(classificationService.getCurrentActive(supplierId));
    }

    /**
     * 查询分级历史 / Query classification history
     */
    @GetMapping("/{supplierId}/history")
    @Operation(summary = "查询供应商分级历史 / Query classification history")
    @ApiOperationSupport(order = 4)
    public Result<List<SupplierClassification>> history(@PathVariable Long supplierId) {
        return Result.success(classificationService.listHistory(supplierId));
    }

    /**
     * 查询即将到期的分级 / Query classifications nearing expiry
     */
    @GetMapping("/expiring-soon")
    @Operation(summary = "查询即将到期的分级（重评提醒） / Query expiring classifications")
    @ApiOperationSupport(order = 5)
    public Result<List<SupplierClassification>> expiringSoon() {
        return Result.success(classificationService.listExpiringSoon());
    }
}
