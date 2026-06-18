package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.supplier.entity.Bom;
import com.atlas.supplier.entity.BomItem;
import com.atlas.supplier.service.BomService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * BOM 管理 Controller / BOM management Controller
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/material/bom")
@RequiredArgsConstructor
public class BomController {

    private final BomService bomService;

    /** 创建 BOM / Create BOM */
    @PostMapping
    @RequirePermission("material:bom:manage")
    public Result<Bom> create(@RequestBody Bom bom) {
        return Result.success(bomService.create(bom));
    }

    /** 根据 ID 查询 BOM / Query BOM by ID */
    @GetMapping("/{id}")
    @RequirePermission("material:bom:view")
    public Result<Bom> getById(@PathVariable Long id) {
        return Result.success(bomService.getById(id));
    }

    /** 分页查询 BOM / Paginated query of BOMs */
    @GetMapping("/page")
    @RequirePermission("material:bom:view")
    public Result<PageResult<Bom>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Bom> result = bomService.page(keyword, status, page, size);
        return Result.success(PageResult.of(result));
    }

    /** 添加 BOM 物料项 / Add BOM item */
    @PostMapping("/{bomId}/item")
    @RequirePermission("material:bom:manage")
    public Result<BomItem> addItem(@PathVariable Long bomId, @RequestBody BomItem item) {
        item.setBomId(bomId);
        return Result.success(bomService.addItem(item));
    }

    /** 删除 BOM 物料项 / Remove BOM item */
    @DeleteMapping("/item/{itemId}")
    @RequirePermission("material:bom:manage")
    public Result<Void> removeItem(@PathVariable Long itemId) {
        bomService.removeItem(itemId);
        return Result.success();
    }

    /** 查询 BOM 物料项列表 / List BOM items */
    @GetMapping("/{bomId}/items")
    @RequirePermission("material:bom:view")
    public Result<List<BomItem>> listItems(@PathVariable Long bomId) {
        return Result.success(bomService.listItems(bomId));
    }

    /** 估算 BOM 成本 / Estimate BOM cost */
    @GetMapping("/{bomId}/cost")
    @RequirePermission("material:bom:view")
    public Result<BigDecimal> estimateCost(@PathVariable Long bomId) {
        return Result.success(bomService.estimateCost(bomId));
    }

    /** 发布 BOM / Publish BOM */
    @PutMapping("/{id}/publish")
    @RequirePermission("material:bom:manage")
    public Result<Void> publish(@PathVariable Long id) {
        bomService.publish(id);
        return Result.success();
    }

    /** 归档 BOM / Archive BOM */
    @PutMapping("/{id}/archive")
    @RequirePermission("material:bom:manage")
    public Result<Void> archive(@PathVariable Long id) {
        bomService.archive(id);
        return Result.success();
    }
}
