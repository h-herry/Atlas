package com.atlas.supplier.controller.portal;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.dto.portal.DelayReportRequest;
import com.atlas.supplier.dto.portal.DeliveryCommitmentRequest;
import com.atlas.supplier.dto.portal.OrderConfirmRequest;
import com.atlas.supplier.dto.portal.OrderDetailRequest;
import com.atlas.supplier.dto.portal.OrderFulfillmentRequest;
import com.atlas.supplier.dto.portal.ProductionProgressRequest;
import com.atlas.supplier.entity.DeliveryOrder;
import com.atlas.supplier.service.portal.PortalOrderService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 订单管理控制器（供应商端） — 采购订单确认、履行状态、工作台概览 /
 * Order management controller (portal) — purchase order confirmation, fulfillment status, dashboard overview
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/portal/orders")
@RequiredArgsConstructor
public class PortalOrderController {

    private final PortalOrderService portalOrderService;

    /**
     * 采购订单列表（企业下达的订单，分页、状态筛选） /
     * Purchase order list (orders placed by enterprises, paginated, status filter)
     */
    @GetMapping
    @RequirePermission("supplier:portal:order:view")
    public Result<Page<DeliveryOrder>> listOrders(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.ok(portalOrderService.listOrders(page, size, status));
    }

    /**
     * 订单详情（物料、数量、单价、交期、收货信息） /
     * Order detail (material, quantity, unit price, delivery date, receiving info)
     */
    @GetMapping("/{id}")
    @RequirePermission("supplier:portal:order:view")
    public Result<DeliveryOrder> getOrderDetail(@PathVariable Long id) {
        return Result.ok(portalOrderService.getOrderDetail(id));
    }

    /**
     * 确认接单 / Confirm order acceptance
     */
    @PostMapping("/{id}/confirm")
    @RequirePermission("supplier:portal:order:confirm")
    public Result<Void> confirmOrder(@PathVariable Long id,
                                      @Valid @RequestBody OrderConfirmRequest request) {
        portalOrderService.confirmOrder(id, request);
        return Result.ok();
    }

    /**
     * 拒绝接单（含原因） / Reject order (with reason)
     */
    @PostMapping("/{id}/reject")
    @RequirePermission("supplier:portal:order:confirm")
    public Result<Void> rejectOrder(@PathVariable Long id,
                                     @Valid @RequestBody OrderConfirmRequest request) {
        portalOrderService.rejectOrder(id, request);
        return Result.ok();
    }

    /**
     * 更新履行状态（生产中/已部分发货/已全部发货） /
     * Update fulfillment status (producing / partially shipped / fully shipped)
     */
    @PutMapping("/{id}/fulfillment")
    @RequirePermission("supplier:portal:order:fulfill")
    public Result<Void> updateFulfillment(@PathVariable Long id,
                                           @Valid @RequestBody OrderFulfillmentRequest request) {
        portalOrderService.updateFulfillment(id, request);
        return Result.ok();
    }

    /**
     * 订单统计（待确认数、履行中数、已完成数、逾期数） /
     * Order statistics (pending confirm, in progress, completed, overdue)
     */
    @GetMapping("/statistics")
    @RequirePermission("supplier:portal:order:view")
    public Result<Map<String, Object>> getOrderStatistics() {
        return Result.ok(portalOrderService.getOrderStatistics());
    }

    /**
     * 供应商工作台概览（今日待办：待确认订单/待发货/待签合同/待报价） /
     * Supplier dashboard overview (today's todos: pending orders / shipments / contracts / quotes)
     */
    @GetMapping("/dashboard")
    @RequirePermission("supplier:portal:order:view")
    public Result<Map<String, Object>> getDashboard() {
        return Result.ok(portalOrderService.getDashboard());
    }

    // ==================== 订单明细与进度管理 / Order Details & Progress Management ====================

    /**
     * 供应商填写订单详细信息（物料规格、单价、交期、包装等） /
     * Supplier fills order details (material spec, unit price, delivery date, packaging etc.)
     */
    @PutMapping("/{id}/details")
    @RequirePermission("supplier:portal:order:fulfill")
    public Result<Void> fillOrderDetails(@PathVariable Long id,
                                          @Valid @RequestBody OrderDetailRequest request) {
        portalOrderService.fillOrderDetails(id, request);
        return Result.ok();
    }

    /**
     * 更新生产进度（完成百分比、当前阶段、质检状态） /
     * Update production progress (completion percentage, current stage, QC status)
     */
    @PutMapping("/{id}/progress")
    @RequirePermission("supplier:portal:order:fulfill")
    public Result<Void> updateProductionProgress(@PathVariable Long id,
                                                  @Valid @RequestBody ProductionProgressRequest request) {
        portalOrderService.updateProductionProgress(id, request);
        return Result.ok();
    }

    /**
     * 供应商承诺交期（确认具体交付日期及生产计划） /
     * Supplier commits delivery date (confirm specific delivery date and production plan)
     */
    @PostMapping("/{id}/delivery-commitment")
    @RequirePermission("supplier:portal:order:fulfill")
    public Result<Void> commitDeliveryDate(@PathVariable Long id,
                                            @Valid @RequestBody DeliveryCommitmentRequest request) {
        portalOrderService.commitDeliveryDate(id, request);
        return Result.ok();
    }

    /**
     * 交期延迟报备（延迟原因 + 新预计交期 + 影响说明） /
     * Report delivery delay (delay reason + new estimated date + impact description)
     */
    @PostMapping("/{id}/delivery-delay")
    @RequirePermission("supplier:portal:order:fulfill")
    public Result<Void> reportDelay(@PathVariable Long id,
                                     @Valid @RequestBody DelayReportRequest request) {
        portalOrderService.reportDelay(id, request);
        return Result.ok();
    }

    /**
     * 获取订单履行时间线（订单从下达到完成的完整状态变化轨迹） /
     * Get order fulfillment timeline (complete status change trajectory from order placement to completion)
     */
    @GetMapping("/{id}/timeline")
    @RequirePermission("supplier:portal:order:view")
    public Result<List<Map<String, Object>>> getOrderFulfillmentTimeline(@PathVariable Long id) {
        return Result.ok(portalOrderService.getOrderFulfillmentTimeline(id));
    }
}
