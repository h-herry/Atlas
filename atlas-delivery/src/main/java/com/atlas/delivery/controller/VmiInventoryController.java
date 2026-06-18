package com.atlas.delivery.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.delivery.entity.VmiInventory;
import com.atlas.delivery.service.VmiInventoryService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * VMI库存监控 REST API / VMI Inventory Monitoring REST API
 * <p>
 * 提供供应商门户库存查询和采购端库存看板接口。 /
 * Provides supplier portal inventory query and procurement dashboard endpoints.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@RestController
@RequestMapping("/api/delivery/vmi")
@RequiredArgsConstructor
@Tag(name = "VMI库存管理 / VMI Inventory Management")
public class VmiInventoryController {

    private final VmiInventoryService vmiService;

    /**
     * 供应商门户端查询本供应商VMI库存 / Supplier portal: query own VMI inventory
     *
     * @param supplierId 供应商ID / Supplier ID
     * @return VMI库存列表 / VMI inventory list
     */
    @GetMapping("/inventory")
    @RequirePermission("delivery:vmi:view")
    public Result<List<VmiInventory>> queryBySupplier(@RequestParam @NotNull Long supplierId) {
        return Result.ok(vmiService.queryBySupplier(supplierId));
    }

    /**
     * 采购端库存看板（按仓库维度分页） / Procurement dashboard (by warehouse, paginated)
     *
     * @param warehouseCode 仓库编码（可选） / Warehouse code (optional)
     * @param page          当前页 / Current page
     * @param size          每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    @GetMapping("/dashboard/warehouse")
    @RequirePermission("delivery:vmi:dashboard")
    public Result<PageResult<VmiInventory>> dashboardByWarehouse(
            @RequestParam(required = false) String warehouseCode,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<VmiInventory> pageResult = vmiService.dashboardByWarehouse(warehouseCode, page, size);
        return PageResult.ok(pageResult.getTotal(), pageResult.getCurrent(), pageResult.getSize(), pageResult.getRecords());
    }

    /**
     * 采购端库存看板（按物料维度汇总） / Procurement dashboard (aggregated by material)
     *
     * @return 物料维度库存汇总 / Material-level summary
     */
    @GetMapping("/dashboard/material")
    @RequirePermission("delivery:vmi:dashboard")
    public Result<List<Map<String, Object>>> dashboardByMaterial() {
        return Result.ok(vmiService.dashboardByMaterial());
    }

    /**
     * 采购端库存看板（按供应商维度汇总） / Procurement dashboard (aggregated by supplier)
     *
     * @return 供应商维度库存汇总 / Supplier-level summary
     */
    @GetMapping("/dashboard/supplier")
    @RequirePermission("delivery:vmi:dashboard")
    public Result<List<Map<String, Object>>> dashboardBySupplier() {
        return Result.ok(vmiService.dashboardBySupplier());
    }

    /**
     * 获取低于安全库存的物料 / Get items below safety stock
     *
     * @return 低库存列表 / Low stock list
     */
    @GetMapping("/low-stock")
    @RequirePermission("delivery:vmi:view")
    public Result<List<VmiInventory>> lowStock() {
        return Result.ok(vmiService.getLowStockItems());
    }

    /**
     * 创建/更新VMI库存记录 / Create or update VMI inventory
     *
     * @param vmi VMI库存实体 / VMI inventory entity
     * @return 保存后的记录 / Saved record
     */
    @PostMapping("/inventory")
    @RequirePermission("delivery:vmi:manage")
    public Result<VmiInventory> save(@Valid @RequestBody VmiInventory vmi) {
        return Result.ok(vmiService.saveOrUpdate(vmi));
    }
}
