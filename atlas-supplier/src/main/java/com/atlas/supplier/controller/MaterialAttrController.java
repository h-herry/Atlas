package com.atlas.supplier.controller;

import com.atlas.common.core.web.Result;
import com.atlas.supplier.entity.MaterialAttrTemplate;
import com.atlas.supplier.service.MaterialAttrService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 物料规格属性 Controller — REST API for material attribute template management /
 * 物料规格属性 Controller — 物料规格属性模板管理的 REST API 端点
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/material/attr")
@RequiredArgsConstructor
@Tag(name = "物料规格属性 / Material Attributes")
public class MaterialAttrController {

    private final MaterialAttrService materialAttrService;

    /**
     * 按分类获取属性模板（含继承） / Query attribute templates by category (with inheritance)
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "按分类获取属性模板（含继承） / Get attributes by category (with inheritance)")
    @ApiOperationSupport(order = 1)
    public Result<List<MaterialAttrTemplate>> listByCategory(@PathVariable Long categoryId) {
        return Result.success(materialAttrService.getByCategoryId(categoryId));
    }

    /**
     * 校验属性完整度 / Validate attribute completeness
     */
    @PostMapping("/validate/{categoryId}")
    @Operation(summary = "校验属性完整度 / Validate attribute completeness")
    @ApiOperationSupport(order = 2)
    public Result<List<String>> validate(@PathVariable Long categoryId,
                                          @RequestBody Map<String, String> attrValues) {
        return Result.success(materialAttrService.validateAttributes(categoryId, attrValues));
    }

    /**
     * 新增属性模板 / Create attribute template
     */
    @PostMapping
    @Operation(summary = "新增属性模板 / Create attribute template")
    @ApiOperationSupport(order = 3)
    public Result<MaterialAttrTemplate> create(@RequestBody MaterialAttrTemplate template) {
        return Result.success(materialAttrService.create(template));
    }

    /**
     * 更新属性模板 / Update attribute template
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新属性模板 / Update attribute template")
    @ApiOperationSupport(order = 4)
    public Result<Void> update(@PathVariable Long id, @RequestBody MaterialAttrTemplate template) {
        template.setId(id);
        materialAttrService.update(template);
        return Result.success();
    }

    /**
     * 删除属性模板 / Delete attribute template
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除属性模板 / Delete attribute template")
    @ApiOperationSupport(order = 5)
    public Result<Void> delete(@PathVariable Long id) {
        materialAttrService.delete(id);
        return Result.success();
    }
}
