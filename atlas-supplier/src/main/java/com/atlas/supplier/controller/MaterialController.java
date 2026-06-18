package com.atlas.supplier.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.PageResult;
import com.atlas.common.web.Result;
import com.atlas.common.entity.Goods;
import com.atlas.common.entity.GoodsCategory;
import com.atlas.supplier.service.MaterialService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 物料管理 Controller / Material management Controller
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@RestController
@RequestMapping("/api/material")
@RequiredArgsConstructor
public class MaterialController {

    private final MaterialService materialService;

    // ==================== 物料 CRUD / Material CRUD ====================

    /** 新增物料 / Add material */
    @PostMapping
    @RequirePermission("material:manage")
    public Result<Goods> save(@RequestBody Goods material) {
        return Result.success(materialService.save(material));
    }

    /** 更新物料 / Update material */
    @PutMapping
    @RequirePermission("material:manage")
    public Result<Goods> update(@RequestBody Goods material) {
        return Result.success(materialService.update(material));
    }

    /** 根据 ID 查询物料 / Query material by ID */
    @GetMapping("/{id}")
    @RequirePermission("material:view")
    public Result<Goods> getById(@PathVariable Long id) {
        return Result.success(materialService.getById(id));
    }

    /** 根据编码查询物料 / Query material by code */
    @GetMapping("/code/{code}")
    @RequirePermission("material:view")
    public Result<Goods> getByCode(@PathVariable String code) {
        return Result.success(materialService.getByCode(code));
    }

    /** 分页查询物料 / Paginated query of materials */
    @GetMapping("/page")
    @RequirePermission("material:view")
    public Result<PageResult<Goods>> page(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String materialType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<Goods> result = materialService.page(keyword, categoryId, materialType, status, page, size);
        return Result.success(PageResult.of(result));
    }

    /** 切换物料状态 / Toggle material status */
    @PutMapping("/{id}/status")
    @RequirePermission("material:manage")
    public Result<Void> toggleStatus(@PathVariable Long id, @RequestParam Integer status) {
        materialService.toggleStatus(id, status);
        return Result.success();
    }

    // ==================== 分类管理 / Category Management ====================

    /** 保存物料分类 / Save material category */
    @PostMapping("/category")
    @RequirePermission("material:manage")
    public Result<GoodsCategory> saveCategory(@RequestBody GoodsCategory category) {
        return Result.success(materialService.saveCategory(category));
    }

    /** 分页查询物料分类 / Paginated query of categories */
    @GetMapping("/category/page")
    @RequirePermission("material:view")
    public Result<PageResult<GoodsCategory>> pageCategory(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long parentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Page<GoodsCategory> result = materialService.pageCategory(keyword, parentId, page, size);
        return Result.success(PageResult.of(result));
    }
}
