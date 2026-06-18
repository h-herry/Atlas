package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.SupplierDelivery;
import com.atlas.supplier.entity.SupplierDeliveryItem;
import com.atlas.supplier.service.SupplierDeliveryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 供应商发货协同 Controller / Supplier delivery collaboration Controller
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/material/delivery")
@RequiredArgsConstructor
public class SupplierDeliveryController {

    private final SupplierDeliveryService deliveryService;

    /** 根据 ID 查询发货单 / Query delivery by ID */
    @GetMapping("/{id}")
    @RequirePermission("material:delivery:view")
    public Result<SupplierDelivery> getById(@PathVariable Long id) {
        return Result.success(deliveryService.getById(id));
    }

    /** 分页查询发货单 / Paginated query of deliveries */
    @GetMapping("/page")
    @RequirePermission("material:delivery:view")
    public Result<PageResult<SupplierDelivery>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long supplierId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<SupplierDelivery> result = deliveryService.page(keyword, supplierId, status, page, size);
        return Result.success(PageResult.of(result));
    }

    /** 发货（填写物流单号） / Ship (fill tracking number) */
    @PutMapping("/{id}/ship")
    @RequirePermission("material:delivery:manage")
    public Result<Void> ship(@PathVariable Long id,
                              @RequestParam String trackingNo,
                              @RequestParam(required = false) String carrier) {
        deliveryService.ship(id, trackingNo, carrier);
        return Result.success();
    }

    /** 收货确认 / Confirm receipt */
    @PutMapping("/{id}/receive")
    @RequirePermission("material:delivery:manage")
    public Result<Void> receive(@PathVariable Long id) {
        deliveryService.receive(id);
        return Result.success();
    }

    /** 添加发货明细项 / Add delivery item */
    @PostMapping("/{deliveryId}/item")
    @RequirePermission("material:delivery:manage")
    public Result<SupplierDeliveryItem> addItem(@PathVariable Long deliveryId,
                                                  @RequestBody SupplierDeliveryItem item) {
        item.setDeliveryId(deliveryId);
        return Result.success(deliveryService.addItem(item));
    }

    /** 查询发货明细 / List delivery items */
    @GetMapping("/{deliveryId}/items")
    @RequirePermission("material:delivery:view")
    public Result<List<SupplierDeliveryItem>> listItems(@PathVariable Long deliveryId) {
        return Result.success(deliveryService.listItems(deliveryId));
    }
}
