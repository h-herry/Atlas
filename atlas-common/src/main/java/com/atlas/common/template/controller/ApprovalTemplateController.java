package com.atlas.common.template.controller;

import com.atlas.common.template.entity.ApprovalTemplate;
import com.atlas.common.template.service.ApprovalTemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审批流模板 Controller — 预置审批链 CRUD + 按模块查询 /
 * Approval template Controller — preset approval chain CRUD + module query
 *
 * @since 1.2.22
 */
@RestController
@RequestMapping("/api/system/approval-template")
@RequiredArgsConstructor
public class ApprovalTemplateController {

    private final ApprovalTemplateService templateService;

    /**
     * 创建模板 / Create template
     */
    @PostMapping
    public ApprovalTemplate create(@RequestBody ApprovalTemplate template) {
        return templateService.create(template);
    }

    /**
     * 按模块查询模板 / Query templates by module
     */
    @GetMapping
    public List<ApprovalTemplate> list(@RequestParam String module) {
        return templateService.listByModule(module);
    }

    /**
     * 获取模块默认模板 / Get default template for module
     */
    @GetMapping("/default")
    public ApprovalTemplate getDefault(@RequestParam String module) {
        return templateService.getDefault(module);
    }

    /**
     * 按ID查模板 / Get template by ID
     */
    @GetMapping("/{templateId}")
    public ApprovalTemplate getById(@PathVariable Long templateId) {
        return templateService.getById(templateId);
    }

    /**
     * 更新模板 / Update template
     */
    @PutMapping("/{templateId}")
    public ApprovalTemplate update(@PathVariable Long templateId,
                                    @RequestBody ApprovalTemplate template) {
        return templateService.update(templateId, template);
    }

    /**
     * 删除模板 / Delete template
     */
    @DeleteMapping("/{templateId}")
    public void delete(@PathVariable Long templateId) {
        templateService.delete(templateId);
    }
}
