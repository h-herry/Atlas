package com.atlas.purchase.order.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.purchase.order.entity.SupplierPlantRel;
import com.atlas.purchase.order.service.PlantAllocationService;
import com.atlas.purchase.order.service.PlantAllocationService.PlantDemand;
import com.atlas.purchase.order.service.PlantAllocationService.AllocationResult;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 多工厂分单 REST API / Multi-Plant Order Allocation REST API
 * <p>
 * 提供工厂级供应商查询和多工厂需求分单接口。 /
 * Provides plant-level supplier query and multi-plant demand allocation endpoints.
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@RestController
@RequestMapping("/api/order/allocation")
@RequiredArgsConstructor
@Tag(name = "分配管理 / Allocation Management")
public class AllocationController {

    private final PlantAllocationService allocationService;

    /**
     * 查询工厂可用供应商 / Query available suppliers for a plant
     * <p>
     * 返回按优先级+提前期排序的供应商列表。 /
     * Returns supplier list sorted by priority + lead time.
     *
     * @param plantCode 工厂编码 / Plant code
     * @return 供应商关联列表 / Supplier relation list
     */
    @GetMapping("/suppliers")
    @RequirePermission("order:allocation:view")
    public Result<List<SupplierPlantRel>> getSuppliers(@RequestParam @NotBlank String plantCode) {
        return Result.ok(allocationService.getAvailableSuppliers(plantCode));
    }

    /**
     * 多工厂需求分单 / Multi-plant demand allocation
     * <p>
     * 输入多工厂需求列表，按供应商产能和优先级自动拆分订单。 /
     * Input multi-plant demand list; automatically split orders by supplier capacity and priority.
     *
     * @param demands 需求列表 / Demand list
     * @return 分单结果（含已分配和未分配） / Allocation result (allocated + unallocated)
     */
    @PostMapping("/allocate")
    @RequirePermission("order:allocation:manage")
    public Result<AllocationResult> allocate(@Valid @RequestBody @NotEmpty List<PlantDemand> demands) {
        return Result.ok(allocationService.allocate(demands));
    }
}
