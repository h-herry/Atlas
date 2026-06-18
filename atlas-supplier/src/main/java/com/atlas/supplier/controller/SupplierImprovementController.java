package com.atlas.supplier.controller;

import com.atlas.common.core.web.Result;
import com.atlas.supplier.entity.SupplierImprovement;
import com.atlas.supplier.service.SupplierImprovementService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 供应商改善跟踪 Controller — REST API for supplier improvement tracking (closed-loop) /
 * 供应商改善跟踪 Controller — 供应商改善跟踪闭环管理的 REST API 端点
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/supplier/improvement")
@RequiredArgsConstructor
@Tag(name = "供应商改善跟踪 / Supplier Improvement")
public class SupplierImprovementController {

    private final SupplierImprovementService improvementService;

    /**
     * 创建整改单 / Create improvement order
     */
    @PostMapping
    @Operation(summary = "创建整改单 / Create improvement order")
    @ApiOperationSupport(order = 1)
    public Result<SupplierImprovement> create(@RequestBody SupplierImprovement improvement) {
        return Result.success(improvementService.create(improvement));
    }

    /**
     * 记录根因分析 / Record root cause analysis
     */
    @PutMapping("/{id}/root-cause")
    @Operation(summary = "记录根因分析 / Record root cause analysis")
    @ApiOperationSupport(order = 2)
    public Result<Void> recordRootCause(@PathVariable Long id,
                                         @RequestBody Map<String, String> body) {
        improvementService.recordRootCause(id, body.get("rootCause"));
        return Result.success();
    }

    /**
     * 记录纠正措施 / Record corrective action
     */
    @PutMapping("/{id}/corrective-action")
    @Operation(summary = "记录纠正措施并推进到 IN_PROGRESS / Record corrective action -> IN_PROGRESS")
    @ApiOperationSupport(order = 3)
    public Result<Void> recordCorrectiveAction(@PathVariable Long id,
                                                @RequestBody Map<String, String> body) {
        improvementService.recordCorrectiveAction(id, body.get("correctiveAction"));
        return Result.success();
    }

    /**
     * 验证整改 / Verify improvement
     */
    @PutMapping("/{id}/verify")
    @Operation(summary = "验证整改 -> VERIFIED / Verify improvement -> VERIFIED")
    @ApiOperationSupport(order = 4)
    public Result<Void> verify(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        improvementService.verify(id,
            Long.valueOf(body.get("verifierId").toString()),
            (String) body.get("verifierName"));
        return Result.success();
    }

    /**
     * 关闭整改单 / Close improvement order
     */
    @PutMapping("/{id}/close")
    @Operation(summary = "关闭整改单 -> CLOSED / Close improvement -> CLOSED")
    @ApiOperationSupport(order = 5)
    public Result<Void> close(@PathVariable Long id) {
        improvementService.close(id);
        return Result.success();
    }

    /**
     * 按供应商查询 / Query by supplier
     */
    @GetMapping("/supplier/{supplierId}")
    @Operation(summary = "按供应商查询整改单 / Query improvements by supplier")
    @ApiOperationSupport(order = 6)
    public Result<List<SupplierImprovement>> listBySupplier(@PathVariable Long supplierId) {
        return Result.success(improvementService.listBySupplierId(supplierId));
    }

    /**
     * 按状态查询 / Query by status
     */
    @GetMapping("/status/{status}")
    @Operation(summary = "按状态查询整改单 / Query improvements by status")
    @ApiOperationSupport(order = 7)
    public Result<List<SupplierImprovement>> listByStatus(@PathVariable String status) {
        return Result.success(improvementService.listByStatus(status));
    }

    /**
     * 查询超期整改单 / Query overdue improvements
     */
    @GetMapping("/overdue")
    @Operation(summary = "查询超期未关闭整改单 / Query overdue improvements")
    @ApiOperationSupport(order = 8)
    public Result<List<SupplierImprovement>> listOverdue() {
        return Result.success(improvementService.listOverdue());
    }

    /**
     * 整改单详情 / Improvement detail
     */
    @GetMapping("/{id}")
    @Operation(summary = "整改单详情 / Improvement detail")
    @ApiOperationSupport(order = 9)
    public Result<SupplierImprovement> detail(@PathVariable Long id) {
        return Result.success(improvementService.getById(id));
    }
}
