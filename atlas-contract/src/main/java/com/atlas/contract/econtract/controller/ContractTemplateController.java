package com.atlas.contract.econtract.controller;

import com.atlas.common.annotation.AuditLog;
import com.atlas.common.core.util.JwtUtil;
import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.common.web.Result;
import com.atlas.contract.econtract.dto.TemplateDTO;
import com.atlas.contract.econtract.model.CntTemplate;
import com.atlas.contract.econtract.service.ContractTemplateService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 合同模板管理 Controller / Contract template management Controller
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@RestController
@RequestMapping("/api/contract/template")
@RequiredArgsConstructor
@Tag(name = "合同模板管理 / Contract Template Management")
public class ContractTemplateController {

    private final ContractTemplateService templateService;
    private final JwtUtil jwtUtil;

    // ============ 模板 CRUD / Template CRUD ============

    /**
     * 创建合同模板 / Create contract template
     */
    @PostMapping
    @RequirePermission("contract:template:add")
    @AuditLog(module = "CONTRACT_TEMPLATE", operation = "CREATE", description = "创建合同模板")
    public Result<Void> create(@Valid @RequestBody TemplateDTO dto,
                                @RequestHeader("Authorization") String authHeader) {
        dto.setCreatedBy(extractRealName(authHeader));
        templateService.create(dto);
        return Result.ok();
    }

    /**
     * 模板列表（分页 + 分类筛选）/ Template list (paginated + category filter)
     */
    @GetMapping("/list")
    @RequirePermission("contract:template:view")
    public Result<Page<CntTemplate>> list(@RequestParam(required = false) String category,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.ok(templateService.page(category, keyword, page, size));
    }

    /**
     * 获取所有启用的模板 / Get all active templates
     */
    @GetMapping("/active")
    @RequirePermission("contract:template:view")
    public Result<java.util.List<CntTemplate>> listActive() {
        return Result.ok(templateService.listActive());
    }

    /**
     * 获取模板详情 / Get template detail
     */
    @GetMapping("/{id}")
    @RequirePermission("contract:template:view")
    public Result<CntTemplate> get(@PathVariable Long id) {
        return Result.ok(templateService.getById(id));
    }

    /**
     * 更新合同模板 / Update contract template
     */
    @PutMapping("/{id}")
    @RequirePermission("contract:template:edit")
    @AuditLog(module = "CONTRACT_TEMPLATE", operation = "UPDATE", description = "更新合同模板")
    public Result<Void> update(@PathVariable Long id,
                                @Valid @RequestBody TemplateDTO dto) {
        templateService.updateTemplate(id, dto);
        return Result.ok();
    }

    /**
     * 删除合同模板 / Delete contract template
     */
    @DeleteMapping("/{id}")
    @RequirePermission("contract:template:delete")
    @AuditLog(module = "CONTRACT_TEMPLATE", operation = "DELETE", description = "删除合同模板")
    public Result<Void> delete(@PathVariable Long id) {
        templateService.deleteTemplate(id);
        return Result.ok();
    }

    /**
     * 基于模板生成合同 / Generate contract from template
     */
    @PostMapping("/{id}/generate")
    @RequirePermission("contract:template:use")
    @AuditLog(module = "CONTRACT_TEMPLATE", operation = "GENERATE", description = "基于模板生成合同")
    public Result<CntTemplate> generate(@PathVariable Long id) {
        return Result.ok(templateService.generateFromTemplate(id));
    }

    // ============ Token 工具方法 / Token Utility Methods ============

    private String extractRealName(String header) {
        String token = header.substring(7);
        return String.valueOf(jwtUtil.parseToken(token).get("realName", String.class));
    }
}
