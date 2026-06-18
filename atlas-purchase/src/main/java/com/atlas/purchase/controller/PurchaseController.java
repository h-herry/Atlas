package com.atlas.purchase.controller;

import com.atlas.common.annotation.AuditLog;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.purchase.entity.PurchaseOrder;
import com.atlas.purchase.entity.PurchaseOrderItem;
import com.atlas.purchase.model.PurchaseCreateRequest;
import com.atlas.purchase.service.PurchaseService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 采购订单管理 REST API / Purchase order management REST API
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    /**
     * 创建采购单（草稿） / Create purchase order (draft)
     */
    @PostMapping("/order")
    @RequirePermission("purchase:create")
    @AuditLog(module = "PURCHASE", operation = "CREATE", description = "创建采购订单")
    public Result<PurchaseOrder> createOrder(@Valid @RequestBody PurchaseCreateRequest request) {
        PurchaseOrder order = purchaseService.createOrder(request);
        return Result.ok(order);
    }

    /**
     * 提交采购单（幂等 + 库存扣减） / Submit purchase order (idempotent + inventory deduction)
     */
    @PostMapping("/order/{orderId}/submit")
    @RequirePermission("purchase:submit")
    public Result<PurchaseOrder> submitOrder(@PathVariable Long orderId) {
        PurchaseOrder order = purchaseService.submitOrder(orderId);
        return Result.ok(order);
    }

    /**
     * 分页查询采购订单 / Paginated query of purchase orders
     *
     * @param supplierId      供应商ID（可选筛选）
     * @param status          状态（可选筛选）
     * @param procurementType 采购方式（可选筛选）
     * @param page            当前页（默认1）
     * @param size            每页大小（默认10）
     */
    @GetMapping("/order/page")
    @RequirePermission("purchase:view")
    public Result<PageResult<PurchaseOrder>> page(
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Integer procurementType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<PurchaseOrder> pageResult = purchaseService.page(supplierId, status, procurementType, page, size);
        return PageResult.ok(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords());
    }

    /**
     * 查询采购单详情 / Query purchase order detail
     */
    @GetMapping("/order/{orderId}")
    @RequirePermission("purchase:view")
    public Result<PurchaseOrder> getOrder(@PathVariable Long orderId) {
        return Result.ok(purchaseService.getById(orderId));
    }

    /**
     * 查询采购单明细 / Query purchase order items
     */
    @GetMapping("/order/{orderId}/items")
    @RequirePermission("purchase:view")
    public Result<List<PurchaseOrderItem>> listItems(@PathVariable Long orderId) {
        return Result.ok(purchaseService.listItems(orderId));
    }
}
