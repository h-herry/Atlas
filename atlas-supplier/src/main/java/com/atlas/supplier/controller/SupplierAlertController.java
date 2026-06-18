package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.AlertRecord;
import com.atlas.supplier.entity.AlertRule;
import com.atlas.supplier.service.SupplierAlertService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 供应商预警管理 Controller — 预警规则 + 预警记录 /
 * Supplier alert management Controller — alert rules + alert records
 *
 * @author atlas
 */
@RestController
@RequestMapping("/api/supplier/alert")
@RequiredArgsConstructor
@Tag(name = "供应商预警 / Supplier Alert")
public class SupplierAlertController {

    private final SupplierAlertService alertService;

    // ==================== 预警规则 / Alert Rules ====================

    /** 创建预警规则 / Create alert rule */
    @PostMapping("/rule")
    @RequirePermission("supplier:risk:manage")
    public Result<AlertRule> createRule(@RequestBody AlertRule rule) {
        return Result.ok(alertService.createRule(rule));
    }

    /** 更新预警规则 / Update alert rule */
    @PutMapping("/rule")
    @RequirePermission("supplier:risk:manage")
    public Result<AlertRule> updateRule(@RequestBody AlertRule rule) {
        return Result.ok(alertService.updateRule(rule));
    }

    /** 启用/停用预警规则 / Toggle alert rule enabled/disabled */
    @PutMapping("/rule/{ruleId}/toggle")
    @RequirePermission("supplier:risk:manage")
    public Result<Void> toggleRule(@PathVariable Long ruleId, @RequestParam boolean enabled) {
        alertService.toggleRule(ruleId, enabled);
        return Result.ok();
    }

    /** 分页查询预警规则 / Paginated query of alert rules */
    @GetMapping("/rule/page")
    @RequirePermission("supplier:risk:view")
    public Result<Page<AlertRule>> pageRule(@RequestParam(required = false) Integer isEnabled,
                                             @RequestParam(defaultValue = "1") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return Result.ok(alertService.pageRule(isEnabled, page, size));
    }

    // ==================== 预警记录 / Alert Records ====================

    /** 标记预警已读 / Mark alert as read */
    @PutMapping("/record/{recordId}/read")
    @RequirePermission("supplier:risk:view")
    public Result<Void> markRead(@PathVariable Long recordId) {
        alertService.markRead(recordId);
        return Result.ok();
    }

    /** 处理预警 / Handle alert */
    @PutMapping("/record/{recordId}/handle")
    @RequirePermission("supplier:risk:manage")
    public Result<Void> handleAlert(@PathVariable Long recordId, @RequestParam Long handlerId) {
        alertService.handleAlert(recordId, handlerId);
        return Result.ok();
    }

    /** 分页查询预警记录 / Paginated query of alert records */
    @GetMapping("/record/page")
    @RequirePermission("supplier:risk:view")
    public Result<Page<AlertRecord>> pageAlert(@RequestParam(required = false) Long supplierId,
                                                @RequestParam(required = false) Integer isRead,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int size) {
        return Result.ok(alertService.pageAlert(supplierId, isRead, page, size));
    }

    /** 统计未读预警数 / Count unread alerts */
    @GetMapping("/record/unread-count/{supplierId}")
    @RequirePermission("supplier:risk:view")
    public Result<Long> countUnread(@PathVariable Long supplierId) {
        return Result.ok(alertService.countUnreadBySupplier(supplierId));
    }
}
