package com.atlas.contract.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.contract.entity.ContractClause;
import com.atlas.contract.service.ContractClauseService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 合同条款库 Controller — CRUD / 按分类查询 / 版本管理 /
 * Contract clause library Controller — CRUD / category query / version management
 *
 * @since 1.2.22
 */
@RestController
@RequestMapping("/api/contract/clause")
@RequiredArgsConstructor
@Tag(name = "合同条款管理 / Contract Clause Management")
public class ContractClauseController {

    private final ContractClauseService clauseService;

    /**
     * 创建条款 / Create clause
     */
    @PostMapping
    @RequirePermission("contract:clause:manage")
    public ContractClause create(@RequestBody ContractClause clause) {
        return clauseService.create(clause);
    }

    /**
     * 按分类查询条款 / Query clauses by category
     */
    @GetMapping
    @RequirePermission("contract:clause:view")
    public Page<ContractClause> list(
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return clauseService.listByCategory(category, page, size);
    }

    /**
     * 按编码查最新版本 / Query latest version by code
     */
    @GetMapping("/code/{clauseCode}")
    @RequirePermission("contract:clause:view")
    public ContractClause getByCode(@PathVariable String clauseCode) {
        return clauseService.getLatestByCode(clauseCode);
    }

    /**
     * 按ID查条款 / Query clause by ID
     */
    @GetMapping("/{clauseId}")
    @RequirePermission("contract:clause:view")
    public ContractClause getById(@PathVariable Long clauseId) {
        return clauseService.getById(clauseId);
    }

    /**
     * 更新条款（版本+1）/ Update clause (increment version)
     */
    @PutMapping("/{clauseId}")
    @RequirePermission("contract:clause:manage")
    public ContractClause update(@PathVariable Long clauseId,
                                  @RequestBody ContractClause update) {
        return clauseService.update(clauseId, update);
    }

    /**
     * 归档条款 / Archive clause
     */
    @DeleteMapping("/{clauseId}")
    @RequirePermission("contract:clause:manage")
    public void archive(@PathVariable Long clauseId) {
        clauseService.archive(clauseId);
    }
}
