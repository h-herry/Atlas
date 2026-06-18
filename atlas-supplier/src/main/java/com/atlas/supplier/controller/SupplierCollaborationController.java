package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.DeliveryOrder;
import com.atlas.supplier.entity.ForecastNotice;
import com.atlas.supplier.entity.Reconciliation;
import com.atlas.supplier.service.SupplierCollaborationService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 供应商协同管理 Controller — 预测计划 + 发货单 + 对账单 /
 * Supplier collaboration management Controller — forecast + delivery + reconciliation
 *
 * @author atlas
 */
@RestController
@RequestMapping("/api/supplier/collab")
@RequiredArgsConstructor
public class SupplierCollaborationController {

    private final SupplierCollaborationService collaborationService;

    // ==================== 预测计划 / Forecast ====================

    /** 发布预测计划 / Publish forecast */
    @PostMapping("/forecast")
    @RequirePermission("supplier:collab:add")
    public Result<ForecastNotice> publishForecast(@RequestBody ForecastNotice forecast) {
        return Result.ok(collaborationService.publishForecast(forecast));
    }

    /** 分页查询预测计划 / Paginated query of forecasts */
    @GetMapping("/forecast/page")
    @RequirePermission("supplier:collab:view")
    public Result<Page<ForecastNotice>> pageForecast(@RequestParam(required = false) Long supplierId,
                                                      @RequestParam(defaultValue = "1") int page,
                                                      @RequestParam(defaultValue = "10") int size) {
        return Result.ok(collaborationService.pageForecast(supplierId, page, size));
    }

    // ==================== 发货单 / Delivery ====================

    /** 创建发货单 / Create delivery */
    @PostMapping("/delivery")
    @RequirePermission("supplier:collab:add")
    public Result<DeliveryOrder> createDelivery(@RequestBody DeliveryOrder delivery) {
        return Result.ok(collaborationService.createDelivery(delivery));
    }

    /** 更新物流信息 / Update logistics info */
    @PutMapping("/delivery/{deliveryId}/logistics")
    @RequirePermission("supplier:collab:add")
    public Result<Void> updateLogistics(@PathVariable Long deliveryId,
                                         @RequestParam String logisticsCompany,
                                         @RequestParam String trackingNo,
                                         @RequestParam Integer status) {
        collaborationService.updateLogistics(deliveryId, logisticsCompany, trackingNo, status);
        return Result.ok();
    }

    /** 到货确认 / Confirm arrival */
    @PutMapping("/delivery/{deliveryId}/arrival")
    @RequirePermission("supplier:collab:confirm")
    public Result<Void> confirmArrival(@PathVariable Long deliveryId) {
        collaborationService.confirmArrival(deliveryId);
        return Result.ok();
    }

    /** 分页查询发货单 / Paginated query of deliveries */
    @GetMapping("/delivery/page")
    @RequirePermission("supplier:collab:view")
    public Result<Page<DeliveryOrder>> pageDelivery(@RequestParam(required = false) Long supplierId,
                                                     @RequestParam(required = false) Integer status,
                                                     @RequestParam(defaultValue = "1") int page,
                                                     @RequestParam(defaultValue = "10") int size) {
        return Result.ok(collaborationService.pageDelivery(supplierId, status, page, size));
    }

    // ==================== 对账单 / Reconciliation ====================

    /** 生成对账单 / Create reconciliation */
    @PostMapping("/reconciliation")
    @RequirePermission("supplier:collab:add")
    public Result<Reconciliation> createReconciliation(@RequestBody Reconciliation reconciliation) {
        return Result.ok(collaborationService.createReconciliation(reconciliation));
    }

    /** 供应商确认对账单 / Supplier confirms reconciliation */
    @PutMapping("/reconciliation/{id}/supplier-confirm")
    @RequirePermission("supplier:collab:confirm")
    public Result<Void> supplierConfirm(@PathVariable Long id, @RequestParam String confirmedBy) {
        collaborationService.supplierConfirm(id, confirmedBy);
        return Result.ok();
    }

    /** 采购方确认对账单 / Purchaser confirms reconciliation */
    @PutMapping("/reconciliation/{id}/purchaser-confirm")
    @RequirePermission("supplier:collab:confirm")
    public Result<Void> purchaserConfirm(@PathVariable Long id, @RequestParam String confirmedBy) {
        collaborationService.purchaserConfirm(id, confirmedBy);
        return Result.ok();
    }

    /** 标记已开票 / Mark invoiced */
    @PutMapping("/reconciliation/{id}/invoice")
    @RequirePermission("supplier:collab:add")
    public Result<Void> markInvoiced(@PathVariable Long id) {
        collaborationService.markInvoiced(id);
        return Result.ok();
    }

    /** 分页查询对账单 / Paginated query of reconciliations */
    @GetMapping("/reconciliation/page")
    @RequirePermission("supplier:collab:view")
    public Result<Page<Reconciliation>> pageReconciliation(@RequestParam(required = false) Long supplierId,
                                                            @RequestParam(required = false) Integer status,
                                                            @RequestParam(defaultValue = "1") int page,
                                                            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(collaborationService.pageReconciliation(supplierId, status, page, size));
    }
}
