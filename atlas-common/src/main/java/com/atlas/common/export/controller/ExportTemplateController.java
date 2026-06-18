package com.atlas.common.export.controller;

import com.atlas.common.export.entity.ExportTemplate;
import com.atlas.common.export.service.ExportService;
import com.atlas.common.security.annotation.RequirePermission;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 数据导出模板 Controller — 模板管理 + 按模板导出 /
 * Export template Controller — template management + export by template
 *
 * @since 1.2.22
 */
@RestController
@RequestMapping("/api/system/export-template")
@RequiredArgsConstructor
@Tag(name = "导出模板管理 / Export Template Management")
public class ExportTemplateController {

    private final ExportService exportService;

    /**
     * 创建模板 / Create template
     */
    @PostMapping
    public ExportTemplate create(@RequestBody ExportTemplate template) {
        return exportService.createTemplate(template);
    }

    /**
     * 按模块查模板 / List templates by module
     */
    @GetMapping
    public List<ExportTemplate> list(@RequestParam(required = false) String module) {
        return exportService.listTemplates(module);
    }

    /**
     * 按ID查模板 / Get template by ID
     */
    @GetMapping("/{templateId}")
    @RequirePermission("system:template:view")
    public ExportTemplate getById(@PathVariable Long templateId) {
        return exportService.getTemplateById(templateId);
    }

    /**
     * 按模板导出 / Export by template
     */
    @PostMapping("/{templateId}/export")
    public String export(@PathVariable Long templateId) {
        return exportService.exportByTemplate(templateId);
    }

    /**
     * 删除模板 / Delete template
     */
    @DeleteMapping("/{templateId}")
    public void delete(@PathVariable Long templateId) {
        exportService.deleteTemplate(templateId);
    }
}
