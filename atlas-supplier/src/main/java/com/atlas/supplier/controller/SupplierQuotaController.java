package com.atlas.supplier.controller;

import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.supplier.entity.*;
import com.atlas.supplier.service.*;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商配额管理 Controller / Supplier quota management Controller
 *
 * @author atlas
 */
@RestController
@RequestMapping("/api/supplier/quota")
@RequiredArgsConstructor
public class SupplierQuotaController {

    private final SupplierQuotaService quotaService;

    /** 分页查询配额 / Paginated query of quotas */
    @GetMapping("/page")
    @RequirePermission("supplier:quota:view")
    public Result<Page<SupplierQuota>> pageQuota(@RequestParam(required = false) Long supplierId,
                                                  @RequestParam(defaultValue = "1") int page,
                                                  @RequestParam(defaultValue = "10") int size) {
        return Result.ok(quotaService.page(supplierId, page, size));
    }

    /** 新增配额 / Add quota */
    @PostMapping
    @RequirePermission("supplier:quota:add")
    public Result<SupplierQuota> addQuota(@RequestBody SupplierQuota quota) {
        return Result.ok(quotaService.addQuota(quota));
    }

    /** 更新配额 / Update quota */
    @PutMapping
    @RequirePermission("supplier:quota:edit")
    public Result<SupplierQuota> updateQuota(@RequestBody SupplierQuota quota) {
        return Result.ok(quotaService.updateQuota(quota));
    }

    /** 批量更新配额状态 / Batch update quota status */
    @PutMapping("/batch/status")
    @RequirePermission("supplier:quota:edit")
    public Result<Void> batchUpdateStatus(@RequestParam java.util.List<Long> ids,
                                           @RequestParam Integer quotaStatus) {
        quotaService.batchUpdateStatus(ids, quotaStatus);
        return Result.ok();
    }
}

// ====================================================================
//  供应商整改跟踪 / Supplier Rectification Tracking
// ====================================================================

@RestController
@RequestMapping("/api/supplier/rectification")
@RequiredArgsConstructor
class SupplierRectificationController {

    private final SupplierRectificationService rectificationService;

    /** 分页查询整改 / Paginated query of rectifications */
    @GetMapping("/page")
    @RequirePermission("supplier:rectification:view")
    public Result<Page<SupplierRectification>> page(@RequestParam(required = false) Long supplierId,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return Result.ok(rectificationService.page(supplierId, status, page, size));
    }

    /** 创建整改 / Create rectification */
    @PostMapping
    @RequirePermission("supplier:rectification:add")
    public Result<SupplierRectification> create(@RequestBody SupplierRectification rect) {
        return Result.ok(rectificationService.create(rect));
    }

    /** 提交整改方案 / Submit rectification plan */
    @PutMapping("/{id}/submit-plan")
    @RequirePermission("supplier:rectification:add")
    public Result<Void> submitPlan(@PathVariable Long id,
                                    @RequestParam String plan,
                                    @RequestParam String submitter) {
        rectificationService.submitPlan(id, plan, submitter);
        return Result.ok();
    }

    /** 复评 / Re-evaluate */
    @PutMapping("/{id}/re-evaluate")
    @RequirePermission("supplier:rectification:approve")
    public Result<Void> reEvaluate(@PathVariable Long id,
                                    @RequestParam Integer result,
                                    @RequestParam String evaluator) {
        rectificationService.reEvaluate(id, result, evaluator);
        return Result.ok();
    }
}

// ====================================================================
//  结算单管理 / Settlement Management
// ====================================================================

@RestController
@RequestMapping("/api/supplier/settlement")
@RequiredArgsConstructor
class SettlementController {

    private final SettlementService settlementService;

    /** 分页查询结算单 / Paginated query of settlements */
    @GetMapping("/page")
    @RequirePermission("supplier:settlement:view")
    public Result<Page<SettlementBill>> page(@RequestParam(required = false) Long supplierId,
                                          @RequestParam(required = false) Integer status,
                                          @RequestParam(defaultValue = "1") int page,
                                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(settlementService.page(supplierId, status, page, size));
    }

    /** 生成结算单 / Generate settlement */
    @PostMapping
    @RequirePermission("supplier:settlement:add")
    public Result<SettlementBill> generate(@RequestParam Long reconciliationId) {
        return Result.ok(settlementService.generate(reconciliationId));
    }

    /** 三单匹配 / Three-way match */
    @GetMapping("/{settlementId}/three-way-match")
    @RequirePermission("supplier:settlement:view")
    public Result<SettlementThreeWayMatch> threeWayMatch(@PathVariable Long settlementId) {
        return Result.ok(settlementService.threeWayMatch(settlementId));
    }

    /** 发起结算 / Initiate settlement */
    @PutMapping("/{settlementId}/settle")
    @RequirePermission("supplier:settlement:approve")
    public Result<Void> settle(@PathVariable Long settlementId,
                                @RequestParam String approver) {
        settlementService.settle(settlementId, approver);
        return Result.ok();
    }
}

// ====================================================================
//  需求预测管理 / Demand Forecast Management
// ====================================================================

@RestController
@RequestMapping("/api/supplier/forecast")
@RequiredArgsConstructor
class DemandForecastController {

    private final DemandForecastService forecastService;

    /** 分页查询需求预测 / Paginated query of forecasts */
    @GetMapping("/page")
    @RequirePermission("supplier:forecast:view")
    public Result<Page<DemandForecast>> page(@RequestParam(required = false) Long supplierId,
                                              @RequestParam(defaultValue = "1") int page,
                                              @RequestParam(defaultValue = "10") int size) {
        return Result.ok(forecastService.pageBySupplier(supplierId, page, size));
    }

    /** 创建需求预测 / Create demand forecast */
    @PostMapping
    @RequirePermission("supplier:forecast:add")
    public Result<DemandForecast> create(@RequestBody DemandForecast forecast) {
        return Result.ok(forecastService.create(forecast));
    }

    /** 供应商确认 / Supplier confirmation */
    @PutMapping("/{id}/supplier-confirm")
    @RequirePermission("supplier:forecast:confirm")
    public Result<Void> supplierConfirm(@PathVariable Long id,
                                         @RequestParam String confirmedBy) {
        forecastService.supplierConfirm(id, confirmedBy);
        return Result.ok();
    }

    /** 生成配送计划 / Generate delivery plan */
    @PutMapping("/{id}/delivery-plan")
    @RequirePermission("supplier:forecast:add")
    public Result<Void> deliveryPlan(@PathVariable Long id,
                                      @RequestParam String scheduledDate) {
        forecastService.deliveryPlan(id, scheduledDate);
        return Result.ok();
    }
}
