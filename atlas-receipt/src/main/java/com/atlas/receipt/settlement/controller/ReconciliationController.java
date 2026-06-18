package com.atlas.receipt.settlement.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.receipt.settlement.entity.ReconciliationRecord;
import com.atlas.receipt.settlement.service.ReconciliationService;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 对账管理 REST API / Reconciliation management REST API
 *
 * @author Atlas Team
 * @since 1.2.401
 */
@RestController
@RequestMapping("/api/reconciliation")
@RequiredArgsConstructor
@Tag(name = "对账管理 / Reconciliation Management")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    /**
     * 发起对账 / Initiate reconciliation
     */
    @PostMapping("/{settlementId}")
    @RequirePermission("reconciliation:create")
    public Result<ReconciliationRecord> initiate(@PathVariable Long settlementId) {
        return Result.ok(reconciliationService.initiate(settlementId));
    }

    /**
     * 企业端确认 / Enterprise confirm
     */
    @PutMapping("/{id}/enterprise-confirm")
    @RequirePermission("reconciliation:confirm")
    public Result<ReconciliationRecord> enterpriseConfirm(@PathVariable Long id,
                                                           @RequestParam @NotNull Long userId) {
        return Result.ok(reconciliationService.enterpriseConfirm(id, userId));
    }

    /**
     * 供应商确认 / Supplier confirm
     */
    @PutMapping("/{id}/supplier-confirm")
    @RequirePermission("reconciliation:confirm")
    public Result<ReconciliationRecord> supplierConfirm(@PathVariable Long id,
                                                         @RequestParam @NotNull Long userId) {
        return Result.ok(reconciliationService.supplierConfirm(id, userId));
    }

    /**
     * 供应商异议 / Supplier dispute
     */
    @PutMapping("/{id}/dispute")
    @RequirePermission("reconciliation:confirm")
    public Result<ReconciliationRecord> dispute(@PathVariable Long id,
                                                 @RequestParam @NotBlank String reason) {
        return Result.ok(reconciliationService.dispute(id, reason));
    }
}
