package com.atlas.purchase.controller;

import com.atlas.common.core.web.Result;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.purchase.entity.InquiryTemplate;
import com.atlas.purchase.service.InquiryTemplateService;
import com.github.xiaoymin.knife4j.annotations.ApiOperationSupport;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 询价模板 Controller — REST API for inquiry template management /
 * 询价模板 Controller — 询价模板管理的 REST API 端点
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@RestController
@RequestMapping("/api/inquiry/template")
@RequiredArgsConstructor
@Tag(name = "询价模板 / Inquiry Template")
public class InquiryTemplateController {

    private final InquiryTemplateService inquiryTemplateService;

    /**
     * 创建模板 / Create template
     */
    @PostMapping
    @Operation(summary = "创建询价模板 / Create inquiry template")
    @ApiOperationSupport(order = 1)
    @RequirePermission("purchase:template:manage")
    public Result<InquiryTemplate> create(@RequestBody InquiryTemplate template) {
        return Result.success(inquiryTemplateService.create(template));
    }

    /**
     * 更新模板 / Update template
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新询价模板 / Update inquiry template")
    @ApiOperationSupport(order = 2)
    @RequirePermission("purchase:template:manage")
    public Result<Void> update(@PathVariable Long id, @RequestBody InquiryTemplate template) {
        template.setId(id);
        inquiryTemplateService.update(template);
        return Result.success();
    }

    /**
     * 删除模板 / Delete template
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除询价模板 / Delete inquiry template")
    @ApiOperationSupport(order = 3)
    @RequirePermission("purchase:template:manage")
    public Result<Void> delete(@PathVariable Long id) {
        inquiryTemplateService.delete(id);
        return Result.success();
    }

    /**
     * 模板详情 / Template detail
     */
    @GetMapping("/{id}")
    @Operation(summary = "询价模板详情 / Inquiry template detail")
    @ApiOperationSupport(order = 4)
    @RequirePermission("purchase:template:view")
    public Result<InquiryTemplate> detail(@PathVariable Long id) {
        return Result.success(inquiryTemplateService.getById(id));
    }

    /**
     * 按分类查询模板 / Query templates by category
     */
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "按物料分类查询模板 / Query templates by material category")
    @ApiOperationSupport(order = 5)
    @RequirePermission("purchase:template:view")
    public Result<List<InquiryTemplate>> listByCategory(@PathVariable Long categoryId) {
        return Result.success(inquiryTemplateService.listByCategoryId(categoryId));
    }
}
