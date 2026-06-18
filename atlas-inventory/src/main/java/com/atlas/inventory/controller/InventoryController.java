package com.atlas.inventory.controller;

import com.atlas.common.annotation.AuditLog;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.inventory.entity.Inventory;
import com.atlas.inventory.model.DeductRequest;
import com.atlas.inventory.model.DeductResponse;
import com.atlas.inventory.service.InventoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 库存管理 REST API / Inventory management REST API
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
@Tag(name = "库存管理 / Inventory Management")
public class InventoryController {

    private final InventoryService inventoryService;

    /**
     * 查询库存（按 SKU + 仓库） / Query inventory (by SKU + warehouse)
     */
    @GetMapping
    @RequirePermission("inventory:view")
    public Result<Inventory> getBySkuAndWarehouse(
            @RequestParam Long skuId,
            @RequestParam Long warehouseId) {
        return Result.ok(inventoryService.getBySkuAndWarehouse(skuId, warehouseId));
    }

    /**
     * 查询库存（按主键） / Query inventory (by primary key)
     */
    @GetMapping("/{id}")
    @RequirePermission("inventory:view")
    public Result<Inventory> getById(@PathVariable Long id) {
        return Result.ok(inventoryService.getById(id));
    }

    /**
     * 扣减库存（乐观锁，单条） / Deduct inventory (optimistic lock, single)
     */
    @PostMapping("/deduct")
    @RequirePermission("inventory:deduct")
    public Result<DeductResponse> deduct(@Valid @RequestBody DeductRequest request) {
        DeductResponse response = inventoryService.deduct(request);
        if (Boolean.TRUE.equals(response.getSuccess())) {
            return Result.ok(response);
        }
        return Result.fail(5001, response.getMessage());
    }

    /**
     * 入库增加库存 / Add stock (inbound)
     */
    @PostMapping("/add-stock")
    @RequirePermission("inventory:add")
    public Result<Inventory> addStock(
            @RequestParam Long skuId,
            @RequestParam Long warehouseId,
            @RequestParam BigDecimal qty,
            @RequestParam(defaultValue = "0") Long operatorId) {
        return Result.ok(inventoryService.addStock(skuId, warehouseId, qty, operatorId));
    }

    /**
     * 根据订单号入库（收货回调） / Inbound by order number (receipt callback)
     */
    @PostMapping("/add-stock/order")
    @RequirePermission("inventory:add")
    public Result<Inventory> addStockByOrderNo(
            @RequestParam Long skuId,
            @RequestParam Long warehouseId,
            @RequestParam BigDecimal qty,
            @RequestParam String orderNo,
            @RequestParam(defaultValue = "0") Long operatorId) {
        return Result.ok(inventoryService.addStockByOrderNo(skuId, warehouseId, qty, orderNo, operatorId));
    }

    /**
     * 分页查询库存 / Paginated inventory query
     */
    @GetMapping("/page")
    @RequirePermission("inventory:view")
    public Result<PageResult<Inventory>> page(
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Inventory> pageResult = inventoryService.page(warehouseId, page, size);
        return PageResult.ok(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords());
    }

    /**
     * 低库存预警列表 / Low stock alert list
     */
    @GetMapping("/low-stock")
    @RequirePermission("inventory:view")
    public Result<List<Inventory>> listLowStock() {
        return Result.ok(inventoryService.listLowStock());
    }
}
