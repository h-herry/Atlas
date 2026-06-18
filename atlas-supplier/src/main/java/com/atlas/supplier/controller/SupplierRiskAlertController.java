package com.atlas.supplier.controller;

import com.atlas.common.core.web.Result;
import com.atlas.supplier.entity.SupplierRiskAlert;
import com.atlas.supplier.service.SupplierRiskAlertService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 供应商风险预警 Controller — REST API for supplier risk alert management /
 * 供应商风险预警 Controller — 供应商风险预警管理的 REST API 端点
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/supplier/risk-alert")
@RequiredArgsConstructor
@Tag(name = "供应商风险预警 / Supplier Risk Alert")
public class SupplierRiskAlertController {

    private final SupplierRiskAlertService riskAlertService;

    /**
     * 录入风险预警 / Create risk alert
     */
    @PostMapping
    @Operation(summary = "录入风险预警 / Create risk alert")
    @ApiOperationSupport(order = 1)
    public Result<SupplierRiskAlert> create(@RequestBody SupplierRiskAlert alert) {
        return Result.success(riskAlertService.create(alert));
    }

    /**
     * 批量录入 / Batch import risk alerts
     */
    @PostMapping("/batch")
    @Operation(summary = "批量录入风险预警 / Batch import risk alerts")
    @ApiOperationSupport(order = 2)
    public Result<Void> batchCreate(@RequestBody List<SupplierRiskAlert> alerts) {
        riskAlertService.batchCreate(alerts);
        return Result.success();
    }

    /**
     * 按供应商查询 / Query by supplier
     */
    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "按供应商查询风险预警 / Query risk alerts by supplier")
    @ApiOperationSupport(order = 3)
    public Result<List<SupplierRiskAlert>> listBySupplier(@PathVariable Long supplierId) {
        return Result.success(riskAlertService.listBySupplierId(supplierId));
    }

    /**
     * 查询未解决高危风险 / Query unresolved high risks
     */
    @GetMapping("/high-risk")
    @Operation(summary = "查询未解决高危/严重风险 / Query unresolved high/critical risks")
    @ApiOperationSupport(order = 4)
    public Result<List<SupplierRiskAlert>> listHighRisk() {
        return Result.success(riskAlertService.listUnresolvedHighRisk());
    }

    /**
     * 标记已解决 / Mark as resolved
     */
    @PutMapping("/{id}/resolve")
    @Operation(summary = "标记风险已解决 / Mark risk as resolved")
    @ApiOperationSupport(order = 5)
    public Result<Void> resolve(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        riskAlertService.resolve(id,
            Long.valueOf(body.get("handlerId").toString()),
            (String) body.get("handlerName"));
        return Result.success();
    }

    /**
     * 风险详情 / Risk detail
     */
    @GetMapping("/{id}")
    @Operation(summary = "风险预警详情 / Risk alert detail")
    @ApiOperationSupport(order = 6)
    public Result<SupplierRiskAlert> detail(@PathVariable Long id) {
        return Result.success(riskAlertService.getById(id));
    }
}
