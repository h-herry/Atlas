package com.atlas.supplier.controller.portal;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.supplier.dto.portal.DeliveryUpdateRequest;
import com.atlas.supplier.entity.SupplierDelivery;
import com.atlas.supplier.service.portal.PortalDeliveryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * 物流管理控制器（供应商端） — 发货管理、物流轨迹、延迟通知 /
 * Logistics management controller (portal) — shipment management, tracking, delay notification
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@RestController
@RequestMapping("/portal/deliveries")
@RequiredArgsConstructor
public class PortalDeliveryController {

    private final PortalDeliveryService portalDeliveryService;

    /**
     * 发货任务列表（企业已下单待发货） / Delivery task list (enterprise orders pending shipment)
     */
    @GetMapping
    @RequirePermission("supplier:portal:delivery:view")
    public Result<Page<SupplierDelivery>> listDeliveries(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.ok(portalDeliveryService.listDeliveries(page, size, status));
    }

    /**
     * 发货详情（收货地址、物料清单、要求到货日期） /
     * Delivery detail (receiving address, material list, required arrival date)
     */
    @GetMapping("/{id}")
    @RequirePermission("supplier:portal:delivery:view")
    public Result<SupplierDelivery> getDeliveryDetail(@PathVariable Long id) {
        return Result.ok(portalDeliveryService.getDeliveryDetail(id));
    }

    /**
     * 确认发货（录入物流公司、运单号、预计到达时间） /
     * Confirm shipment (enter logistics company, tracking number, estimated arrival)
     */
    @PostMapping("/{id}/ship")
    @RequirePermission("supplier:portal:delivery:ship")
    public Result<Void> ship(@PathVariable Long id,
                              @Valid @RequestBody DeliveryUpdateRequest request) {
        portalDeliveryService.confirmShipment(id, request);
        return Result.ok();
    }

    /**
     * 更新物流轨迹 / Update tracking trajectory
     */
    @PutMapping("/{id}/tracking")
    @RequirePermission("supplier:portal:delivery:tracking")
    public Result<Void> updateTracking(@PathVariable Long id,
                                        @RequestBody Map<String, Object> trackingInfo) {
        portalDeliveryService.updateTracking(id, trackingInfo);
        return Result.ok();
    }

    /**
     * 查看物流轨迹 / View tracking trajectory
     */
    @GetMapping("/{id}/tracking")
    @RequirePermission("supplier:portal:delivery:view")
    public Result<List<Map<String, Object>>> getTracking(@PathVariable Long id) {
        return Result.ok(portalDeliveryService.getTracking(id));
    }

    /**
     * 历史发货记录 / Historical delivery records
     */
    @GetMapping("/history")
    @RequirePermission("supplier:portal:delivery:view")
    public Result<Page<SupplierDelivery>> getDeliveryHistory(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.ok(portalDeliveryService.getDeliveryHistory(page, size));
    }

    /**
     * 延迟通知（告知企业交期延迟原因） / Delay notification (inform enterprise of delivery delay reason)
     */
    @PostMapping("/{id}/delay-notice")
    @RequirePermission("supplier:portal:delivery:ship")
    public Result<Void> notifyDelay(@PathVariable Long id,
                                     @RequestBody Map<String, String> body) {
        String reason = body.get("reason");
        LocalDate newEstimate = body.containsKey("newEstimate")
                ? LocalDate.parse(body.get("newEstimate")) : null;
        portalDeliveryService.notifyDelay(id, reason, newEstimate);
        return Result.ok();
    }
}
