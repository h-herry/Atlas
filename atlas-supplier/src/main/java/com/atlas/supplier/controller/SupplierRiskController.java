package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.RiskEvent;
import com.atlas.supplier.entity.SupplierBlacklist;
import com.atlas.supplier.service.SupplierRiskService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 供应商风险管理 Controller — 风险事件 + 黑名单管理 /
 * Supplier risk management Controller — risk events + blacklist management
 *
 * @author atlas
 */
@RestController
@RequestMapping("/api/supplier/risk")
@RequiredArgsConstructor
@Tag(name = "供应商风险管理 / Supplier Risk")
public class SupplierRiskController {

    private final SupplierRiskService riskService;

    // ==================== 风险事件 / Risk Events ====================

    /** 创建风险事件 / Create risk event */
    @PostMapping("/event")
    @RequirePermission("supplier:risk:manage")
    public Result<RiskEvent> createRiskEvent(@RequestBody RiskEvent event) {
        return Result.ok(riskService.createRiskEvent(event));
    }

    /** 开始处理 / Start handling */
    @PutMapping("/event/{eventId}/handle")
    @RequirePermission("supplier:risk:manage")
    public Result<Void> startHandle(@PathVariable Long eventId,
                                     @RequestParam Long handlerId,
                                     @RequestParam String handlerName) {
        riskService.startHandle(eventId, handlerId, handlerName);
        return Result.ok();
    }

    /** 闭环处理 / Resolve risk */
    @PutMapping("/event/{eventId}/resolve")
    @RequirePermission("supplier:risk:manage")
    public Result<Void> resolveRisk(@PathVariable Long eventId,
                                     @RequestParam Integer targetStatus,
                                     @RequestParam(required = false) String handleResult) {
        riskService.resolveRisk(eventId, targetStatus, handleResult);
        return Result.ok();
    }

    /** 分页查询风险事件 / Paginated query of risk events */
    @GetMapping("/event/page")
    @RequirePermission("supplier:risk:view")
    public Result<Page<RiskEvent>> pageRisk(@RequestParam(required = false) Long supplierId,
                                             @RequestParam(required = false) Integer status,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(riskService.pageRisk(supplierId, status, page, size));
    }

    // ==================== 黑名单 / Blacklist ====================

    /** 加入黑名单 / Add to blacklist */
    @PostMapping("/blacklist")
    @RequirePermission("supplier:risk:manage")
    public Result<SupplierBlacklist> addToBlacklist(@RequestBody SupplierBlacklist blacklist) {
        return Result.ok(riskService.addToBlacklist(blacklist));
    }

    /** 解除黑名单 / Remove from blacklist */
    @PutMapping("/blacklist/{supplierId}/remove")
    @RequirePermission("supplier:risk:manage")
    public Result<Void> removeFromBlacklist(@PathVariable Long supplierId) {
        riskService.removeFromBlacklist(supplierId);
        return Result.ok();
    }

    /** 黑名单校验 / Check blacklist status */
    @GetMapping("/blacklist/check/{supplierId}")
    @RequirePermission("supplier:risk:view")
    public Result<Boolean> checkBlacklist(@PathVariable Long supplierId) {
        return Result.ok(riskService.isBlacklisted(supplierId));
    }

    /** 分页查询黑名单 / Paginated query of blacklist */
    @GetMapping("/blacklist/page")
    @RequirePermission("supplier:risk:view")
    public Result<Page<SupplierBlacklist>> pageBlacklist(@RequestParam(required = false) Integer status,
                                                          @RequestParam(defaultValue = "1") int page,
                                                          @RequestParam(defaultValue = "10") int size) {
        return Result.ok(riskService.pageBlacklist(status, page, size));
    }
}
