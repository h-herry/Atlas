package com.atlas.purchase.controller;

import com.atlas.common.annotation.AuditLog;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.purchase.entity.OrderChange;
import com.atlas.purchase.entity.OrderChangeDetail;
import com.atlas.purchase.entity.PurchaseOrder;
import com.atlas.purchase.entity.PurchaseOrderItem;
import com.atlas.purchase.model.PurchaseCreateRequest;
import com.atlas.purchase.service.OrderChangeService;
import com.atlas.purchase.service.OrderChangeService.ChangeDetailRequest;
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
    private final OrderChangeService orderChangeService;

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
    @AuditLog(module = "PURCHASE", operation = "SUBMIT", description = "提交采购订单 / Submit purchase order")
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

    // ==================== 订单变更管理（ECN） / Order Change Management ====================

    /**
     * 创建变更申请 / Create change request
     */
    @PostMapping("/order/{orderId}/change")
    @RequirePermission("purchase:change")
    @AuditLog(module = "PURCHASE", operation = "CHANGE_CREATE", description = "创建订单变更申请")
    public Result<OrderChange> createChange(@PathVariable Long orderId, @RequestBody CreateChangeRequest request) {
        OrderChange change = orderChangeService.createChange(
                orderId, request.getChangeType(), request.getChangeReason(),
                request.getDetails(), request.getCreatedBy());
        return Result.ok(change);
    }

    /**
     * 提交变更审批 / Submit change for approval
     */
    @PutMapping("/change/{changeId}/submit")
    @RequirePermission("purchase:change")
    @AuditLog(module = "PURCHASE", operation = "CHANGE_SUBMIT", description = "提交变更审批")
    public Result<Void> submitApproval(@PathVariable Long changeId) {
        orderChangeService.submitApproval(changeId);
        return Result.ok();
    }

    /**
     * 审批变更 / Approve or reject change
     */
    @PutMapping("/change/{changeId}/approve")
    @RequirePermission("purchase:change:approve")
    @AuditLog(module = "PURCHASE", operation = "CHANGE_APPROVE", description = "审批变更")
    public Result<Void> approve(@PathVariable Long changeId, @RequestBody ApproveRequest request) {
        orderChangeService.approve(changeId, request.isApproved(), request.getApprovedBy());
        return Result.ok();
    }

    /**
     * 供应商确认变更 / Supplier confirms change
     */
    @PutMapping("/change/{changeId}/supplier-confirm")
    @RequirePermission("purchase:change")
    @AuditLog(module = "PURCHASE", operation = "CHANGE_SUPPLIER_CONFIRM", description = "供应商确认变更")
    public Result<Void> supplierConfirm(@PathVariable Long changeId) {
        orderChangeService.supplierConfirm(changeId);
        return Result.ok();
    }

    /**
     * 执行变更 / Execute change
     */
    @PutMapping("/change/{changeId}/execute")
    @RequirePermission("purchase:change:execute")
    @AuditLog(module = "PURCHASE", operation = "CHANGE_EXECUTE", description = "执行变更")
    public Result<Void> executeChange(@PathVariable Long changeId) {
        orderChangeService.execute(changeId);
        return Result.ok();
    }

    /**
     * 查询变更单详情 / Query change detail
     */
    @GetMapping("/change/{changeId}")
    @RequirePermission("purchase:change:view")
    public Result<OrderChange> getChange(@PathVariable Long changeId) {
        return Result.ok(orderChangeService.getById(changeId));
    }

    /**
     * 查询变更明细列表 / Query change detail items
     */
    @GetMapping("/change/{changeId}/details")
    @RequirePermission("purchase:change:view")
    public Result<List<OrderChangeDetail>> listChangeDetails(@PathVariable Long changeId) {
        return Result.ok(orderChangeService.listDetails(changeId));
    }

    /**
     * 分页查询变更单 / Paginated change query
     */
    @GetMapping("/change/page")
    @RequirePermission("purchase:change:view")
    public Result<Page<OrderChange>> pageChange(
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.ok(orderChangeService.page(orderId, status, page, size));
    }

    // ==================== 请求体 / Request Bodies ====================

    @lombok.Data
    public static class CreateChangeRequest {
        private String changeType;
        private String changeReason;
        private List<ChangeDetailRequest> details;
        private Long createdBy;
    }

    @lombok.Data
    public static class ApproveRequest {
        private boolean approved;
        private Long approvedBy;
    }
}
