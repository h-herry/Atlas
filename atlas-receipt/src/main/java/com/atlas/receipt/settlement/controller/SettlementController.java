package com.atlas.receipt.settlement.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.receipt.settlement.entity.SettlementBill;
import com.atlas.receipt.settlement.entity.SettlementItem;
import com.atlas.receipt.settlement.service.SettlementService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 结算管理 REST API / Settlement management REST API
 *
 * @author Atlas Team
 * @since 1.2.502
 */
@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
@Tag(name = "结算管理 / Settlement Management")
public class SettlementController {

    private final SettlementService settlementService;

    /**
     * 基于已收货订单生成结算单 / Generate settlement from confirmed receipt
     */
    @PostMapping("/generate")
    @RequirePermission("settlement:create")
    public Result<SettlementBill> generate(@RequestParam @NotNull Long receiptId,
                                           @RequestParam @NotNull Long createdBy) {
        return Result.ok(settlementService.generate(receiptId, createdBy));
    }

    /**
     * 结算单详情 / Settlement detail
     */
    @GetMapping("/{id}")
    @RequirePermission("settlement:view")
    public Result<SettlementBill> get(@PathVariable Long id) {
        return Result.ok(settlementService.getById(id));
    }

    /**
     * 分页查询结算单 / Paginated query of settlements
     */
    @GetMapping("/list")
    @RequirePermission("settlement:view")
    public Result<PageResult<SettlementBill>> list(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SettlementBill> pageResult = settlementService.list(supplierId, status, startTime, endTime, page, size);
        return PageResult.ok(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords());
    }

    /**
     * 查询结算明细 / Query settlement items
     */
    @GetMapping("/{id}/items")
    @RequirePermission("settlement:view")
    public Result<List<SettlementItem>> listItems(@PathVariable Long id) {
        return Result.ok(settlementService.listItems(id));
    }

    /**
     * 提交审批 / Submit for approval
     */
    @PutMapping("/{id}/submit")
    @RequirePermission("settlement:submit")
    public Result<SettlementBill> submit(@PathVariable Long id) {
        return Result.ok(settlementService.submit(id));
    }

    /**
     * 审批通过 / Approve
     */
    @PutMapping("/{id}/approve")
    @RequirePermission("settlement:approve")
    public Result<SettlementBill> approve(@PathVariable Long id) {
        return Result.ok(settlementService.approve(id));
    }

    /**
     * 审批驳回 / Reject
     */
    @PutMapping("/{id}/reject")
    @RequirePermission("settlement:approve")
    public Result<SettlementBill> reject(@PathVariable Long id) {
        return Result.ok(settlementService.reject(id));
    }
}
