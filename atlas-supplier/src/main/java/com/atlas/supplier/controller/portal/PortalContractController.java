package com.atlas.supplier.controller.portal;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.service.portal.PortalContractService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 合同管理控制器（供应商端） — 查看合同、在线签署、履约查询、合同预警 /
 * Contract management controller (portal) — view contracts, online signing, performance tracking, contract alerts
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/portal/contracts")
@RequiredArgsConstructor
@Tag(name = "门户合同 / Portal Contract")
public class PortalContractController {

    private final PortalContractService portalContractService;

    /**
     * 查看企业发来的合同列表（分页、状态筛选） / View contract list from enterprises (paginated, status filter)
     */
    @GetMapping
    @RequirePermission("supplier:portal:contract:view")
    public Result<Page<Map<String, Object>>> listContracts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        return Result.ok(portalContractService.listContracts(page, size, status));
    }

    /**
     * 查看合同详情（条款、金额、履约要求） / View contract detail (terms, amount, performance requirements)
     */
    @GetMapping("/{id}")
    @RequirePermission("supplier:portal:contract:view")
    public Result<Map<String, Object>> getContractDetail(@PathVariable Long id) {
        return Result.ok(portalContractService.getContractDetail(id));
    }

    /**
     * 在线签署合同（二次确认 + SMS 验证） / Online contract signing (double confirmation + SMS verification)
     */
    @PostMapping("/{id}/sign")
    @RequirePermission("supplier:portal:contract:sign")
    public Result<Void> signContract(@PathVariable Long id,
                                      @RequestParam String smsCode) {
        portalContractService.signContract(id, smsCode);
        return Result.ok();
    }

    /**
     * 拒绝签署（含原因） / Reject signing (with reason)
     */
    @PostMapping("/{id}/reject")
    @RequirePermission("supplier:portal:contract:sign")
    public Result<Void> rejectContract(@PathVariable Long id,
                                        @RequestBody Map<String, String> body) {
        portalContractService.rejectContract(id, body.get("reason"));
        return Result.ok();
    }

    /**
     * 查看履约进度（交付/付款/质量指标） / View performance progress (delivery/payment/quality metrics)
     */
    @GetMapping("/{id}/performance")
    @RequirePermission("supplier:portal:contract:view")
    public Result<List<Map<String, Object>>> getPerformance(@PathVariable Long id) {
        return Result.ok(portalContractService.getPerformance(id));
    }

    /**
     * 查看合同预警（到期/违约提醒） / View contract alerts (expiry/breach notifications)
     */
    @GetMapping("/{id}/alerts")
    @RequirePermission("supplier:portal:contract:view")
    public Result<List<Map<String, Object>>> getContractAlerts(@PathVariable Long id) {
        return Result.ok(portalContractService.getContractAlerts(id));
    }
}
