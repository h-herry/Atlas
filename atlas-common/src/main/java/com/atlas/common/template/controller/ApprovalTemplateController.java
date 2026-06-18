package com.atlas.common.template.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.template.entity.ApprovalTemplate;
import com.atlas.common.template.service.ApprovalTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 审批流模板 Controller — 预置审批链 CRUD + 按模块查询 /
 * Approval template Controller — preset approval chain CRUD + module query
 *
 * @since 1.2.22
 */
@RestController
@RequestMapping("/api/system/approval-template")
@RequiredArgsConstructor
@Tag(name = "审批模板管理 / Approval Template Management")
public class ApprovalTemplateController {

    private final ApprovalTemplateService templateService;

    /**
     * 创建模板 / Create template
     */
    @PostMapping
    @RequirePermission("system:template:manage")
    public ApprovalTemplate create(@RequestBody ApprovalTemplate template) {
        return templateService.create(template);
    }

    /**
     * 按模块查询模板 / Query templates by module
     */
    @GetMapping
    @RequirePermission("system:template:view")
    public List<ApprovalTemplate> list(@RequestParam String module) {
        return templateService.listByModule(module);
    }

    /**
     * 获取模块默认模板 / Get default template for module
     */
    @GetMapping("/default")
    @RequirePermission("system:template:view")
    public ApprovalTemplate getDefault(@RequestParam String module) {
        return templateService.getDefault(module);
    }

    /**
     * 按ID查模板 / Get template by ID
     */
    @GetMapping("/{templateId}")
    @RequirePermission("system:template:view")
    public ApprovalTemplate getById(@PathVariable Long templateId) {
        return templateService.getById(templateId);
    }

    /**
     * 更新模板 / Update template
     */
    @PutMapping("/{templateId}")
    @RequirePermission("system:template:manage")
    public ApprovalTemplate update(@PathVariable Long templateId,
                                    @RequestBody ApprovalTemplate template) {
        return templateService.update(templateId, template);
    }

    /**
     * 删除模板 / Delete template
     */
    @DeleteMapping("/{templateId}")
    @RequirePermission("system:template:manage")
    public void delete(@PathVariable Long templateId) {
        templateService.delete(templateId);
    }
}
