package com.atlas.receipt.controller;

import com.atlas.common.annotation.AuditLog;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.receipt.entity.Receipt;
import com.atlas.receipt.entity.ReceiptItem;
import com.atlas.receipt.service.ReceiptService;
import com.atlas.receipt.service.ReceiptService.ReceiptItemRequest;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 收货管理 REST API / Receipt management REST API
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/receipt")
@RequiredArgsConstructor
@Tag(name = "收货管理 / Receipt Management")
public class ReceiptController {

    private final ReceiptService receiptService;

    /**
     * 创建收货单 / Create receipt
     */
    @PostMapping
    @RequirePermission("receipt:create")
    public Result<Receipt> createReceipt(@Valid @RequestBody CreateReceiptRequest request) {
        Receipt receipt = receiptService.createReceipt(
                request.getOrderId(),
                request.getSupplierId(),
                request.getWarehouseId(),
                request.getItems(),
                request.getCreatedBy());
        return Result.ok(receipt);
    }

    /**
     * 质检 / Quality inspection
     */
    @PostMapping("/{receiptId}/quality-check")
    @RequirePermission("receipt:check")
    public Result<Receipt> qualityCheck(
            @PathVariable Long receiptId,
            @RequestParam String checkResult,
            @RequestParam Long inspectorId) {
        return Result.ok(receiptService.qualityCheck(receiptId, checkResult, inspectorId));
    }

    /**
     * 确认收货 / Confirm receipt
     */
    @PostMapping("/{receiptId}/confirm")
    @RequirePermission("receipt:confirm")
    @AuditLog(module = "RECEIPT", operation = "CONFIRM", description = "确认收货")
    public Result<Receipt> confirmReceipt(@PathVariable Long receiptId) {
        return Result.ok(receiptService.confirmReceipt(receiptId));
    }

    /**
     * 分页查询收货单 / Paginated query of receipts
     */
    @GetMapping("/page")
    @RequirePermission("receipt:view")
    public Result<PageResult<Receipt>> page(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Receipt> pageResult = receiptService.page(orderId, status, page, size);
        return PageResult.ok(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords());
    }

    /**
     * 查询收货单详情 / Query receipt detail
     */
    @GetMapping("/{receiptId}")
    @RequirePermission("receipt:view")
    public Result<Receipt> get(@PathVariable Long receiptId) {
        return Result.ok(receiptService.getById(receiptId));
    }

    /**
     * 查询收货单明细 / Query receipt items
     */
    @GetMapping("/{receiptId}/items")
    @RequirePermission("receipt:view")
    public Result<List<ReceiptItem>> listItems(@PathVariable Long receiptId) {
        return Result.ok(receiptService.listItems(receiptId));
    }

    // ==================== 请求 DTO / Request DTO ====================

    @lombok.Data
    public static class CreateReceiptRequest {
        @NotNull(message = "采购订单ID不能为空")
        private Long orderId;

        @NotNull(message = "供应商ID不能为空")
        private Long supplierId;

        @NotNull(message = "仓库ID不能为空")
        private Long warehouseId;

        @NotNull(message = "创建人不能为空")
        private Long createdBy;

        @NotEmpty(message = "收货明细不能为空")
        @Valid
        private List<ReceiptItemRequest> items;
    }
}
