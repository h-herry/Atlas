package com.atlas.quality.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.material.entity.LotTrace;
import com.atlas.quality.service.LotTraceService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 来料批次追溯 REST API / Incoming Lot Trace REST API
 * <p>
 * 提供正向追溯（按批次号）和反向追溯（按物料/供应商/订单）接口。 /
 * Provides forward trace (by lot number) and reverse trace (by material/supplier/order) endpoints.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@RestController
@RequestMapping("/api/quality/lot")
@RequiredArgsConstructor
public class LotTraceController {

    private final LotTraceService lotTraceService;

    // ==================== 正向追溯 / Forward Trace ====================

    /**
     * 正向追溯：按批次号查询全链路信息 / Forward trace: query full chain by lot number
     * <p>
     * lot_no → 供应商 / 订单 / 收货日期 / 检验结果 /
     * lot_no → supplier / order / receive date / inspection result
     *
     * @param lotNo 批次号 / Lot number
     * @return 批次全链路追溯信息 / Full chain trace info
     */
    @GetMapping("/forward/{lotNo}")
    @RequirePermission("quality:lot:view")
    public Result<Map<String, Object>> forwardTrace(@PathVariable @NotBlank String lotNo) {
        return Result.ok(lotTraceService.forwardTrace(lotNo));
    }

    // ==================== 反向追溯 / Reverse Trace ====================

    /**
     * 反向追溯：按物料ID查询所有关联批次 / Reverse trace: query all related lots by material ID
     * <p>
     * material_id → 所有关联批次 → 供应商 / 订单列表 /
     * material_id → all related lots → supplier / order list
     *
     * @param materialId 物料ID / Material ID
     * @return 批次列表 / Lot list
     */
    @GetMapping("/reverse/material/{materialId}")
    @RequirePermission("quality:lot:view")
    public Result<List<LotTrace>> reverseByMaterial(@PathVariable @NotNull Long materialId) {
        return Result.ok(lotTraceService.reverseByMaterial(materialId));
    }

    /**
     * 反向追溯：按供应商ID查询所有关联批次 / Reverse trace: query all related lots by supplier ID
     *
     * @param supplierId 供应商ID / Supplier ID
     * @return 批次列表 / Lot list
     */
    @GetMapping("/reverse/supplier/{supplierId}")
    @RequirePermission("quality:lot:view")
    public Result<List<LotTrace>> reverseBySupplier(@PathVariable @NotNull Long supplierId) {
        return Result.ok(lotTraceService.reverseBySupplier(supplierId));
    }

    /**
     * 反向追溯：按订单ID查询所有关联批次 / Reverse trace: query all related lots by order ID
     *
     * @param orderId 订单ID / Order ID
     * @return 批次列表 / Lot list
     */
    @GetMapping("/reverse/order/{orderId}")
    @RequirePermission("quality:lot:view")
    public Result<List<LotTrace>> reverseByOrder(@PathVariable @NotNull Long orderId) {
        return Result.ok(lotTraceService.reverseByOrder(orderId));
    }

    // ==================== 批次管理 / Lot Management ====================

    /**
     * 创建批次记录 / Create lot record
     *
     * @param lotTrace 批次实体 / Lot trace entity
     * @return 创建后的批次 / Created lot
     */
    @PostMapping
    @RequirePermission("quality:lot:manage")
    public Result<LotTrace> create(@RequestBody LotTrace lotTrace) {
        return Result.ok(lotTraceService.createLot(lotTrace));
    }

    /**
     * 质检不合格锁定：按批次号快速锁定受影响范围 / Quick lock affected scope by lot number on inspection failure
     *
     * @param lotNo  批次号 / Lot number
     * @param reason 不合格原因 / Rejection reason
     * @return 受影响的同物料其他批次（供风险评估） / Other lots of same material (for risk assessment)
     */
    @PostMapping("/lock")
    @RequirePermission("quality:lot:manage")
    public Result<Map<String, Object>> lockLot(
            @RequestParam @NotBlank String lotNo,
            @RequestParam @NotBlank String reason) {
        return Result.ok(lotTraceService.lockLot(lotNo, reason));
    }

    /**
     * 按日期范围查询批次 / Query lots by date range
     *
     * @param startDate 开始日期 / Start date
     * @param endDate   结束日期 / End date
     * @param status    状态过滤 / Status filter
     * @param page      当前页 / Current page
     * @param size      每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    @GetMapping
    @RequirePermission("quality:lot:view")
    public Result<PageResult<LotTrace>> list(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<LotTrace> pageResult = lotTraceService.listByDateRange(startDate, endDate, status, page, size);
        return PageResult.ok(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords());
    }
}
