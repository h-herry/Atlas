package com.atlas.supplier.controller;

import com.atlas.common.core.web.Result;
import com.atlas.supplier.entity.MaterialCategory;
import com.atlas.supplier.service.MaterialCategoryService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 物料分类 Controller — REST API for material category management /
 * 物料分类 Controller — 物料分类管理的 REST API 端点
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/material/category")
@RequiredArgsConstructor
@Tag(name = "物料分类管理 / Material Category")
public class MaterialCategoryController {

    private final MaterialCategoryService materialCategoryService;

    /**
     * 查询分类树 / Query category tree
     */
    @GetMapping("/tree")
    @Operation(summary = "查询分类树 / Query category tree")
    @ApiOperationSupport(order = 1)
    public Result<List<MaterialCategory>> tree() {
        return Result.success(materialCategoryService.listAll());
    }

    /**
     * 按层级查询 / Query by level
     */
    @GetMapping("/level/{level}")
    @Operation(summary = "按层级查询分类 / Query categories by level (1-4)")
    @ApiOperationSupport(order = 2)
    public Result<List<MaterialCategory>> listByLevel(@PathVariable Integer level) {
        return Result.success(materialCategoryService.listByLevel(level));
    }

    /**
     * 查询子分类 / Query children
     */
    @GetMapping("/children/{parentId}")
    @Operation(summary = "查询子分类 / Query direct children")
    @ApiOperationSupport(order = 3)
    public Result<List<MaterialCategory>> children(@PathVariable Long parentId) {
        return Result.success(materialCategoryService.listChildren(parentId));
    }

    /**
     * 查询分类详情 / Query category detail
     */
    @GetMapping("/{id}")
    @Operation(summary = "查询分类详情 / Query category detail")
    @ApiOperationSupport(order = 4)
    public Result<MaterialCategory> detail(@PathVariable Long id) {
        return Result.success(materialCategoryService.getById(id));
    }

    /**
     * 新增分类 / Create category
     */
    @PostMapping
    @Operation(summary = "新增物料分类 / Create material category")
    @ApiOperationSupport(order = 5)
    public Result<MaterialCategory> create(@RequestBody MaterialCategory category) {
        return Result.success(materialCategoryService.create(category));
    }

    /**
     * 更新分类 / Update category
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新物料分类 / Update material category")
    @ApiOperationSupport(order = 6)
    public Result<Void> update(@PathVariable Long id, @RequestBody MaterialCategory category) {
        category.setId(id);
        materialCategoryService.update(category);
        return Result.success();
    }

    /**
     * 移动分类 / Move category
     */
    @PutMapping("/{id}/move")
    @Operation(summary = "移动分类到新父节点 / Move category to new parent")
    @ApiOperationSupport(order = 7)
    public Result<Void> move(@PathVariable Long id, @RequestParam Long newParentId) {
        materialCategoryService.move(id, newParentId);
        return Result.success();
    }

    /**
     * 停用分类 / Deactivate category
     */
    @PutMapping("/{id}/deactivate")
    @Operation(summary = "停用物料分类 / Deactivate material category")
    @ApiOperationSupport(order = 8)
    public Result<Void> deactivate(@PathVariable Long id) {
        materialCategoryService.deactivate(id);
        return Result.success();
    }
}
