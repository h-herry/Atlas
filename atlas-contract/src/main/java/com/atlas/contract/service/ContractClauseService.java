package com.atlas.contract.service;

import com.atlas.contract.entity.ContractClause;
import com.atlas.contract.mapper.ContractClauseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * 合同条款库 Service — CRUD / 按分类查询 / 版本管理 /
 * Contract clause library Service — CRUD / category query / version management
 *
 * <p>条款分类: LAW 法律 / COMMERCIAL 商务 / STANDARD 通用 /
 * Clause categories: LAW / COMMERCIAL / STANDARD</p>
 *
 * @since 1.2.22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractClauseService {

    private final ContractClauseMapper clauseMapper;

    /**
     * 创建条款 / Create clause
     */
    @Transactional(rollbackFor = Exception.class)
    public ContractClause create(ContractClause clause) {
        clause.setVersion(1);
        clause.setStatus("ACTIVE");
        clause.setEffectiveDate(LocalDate.now());
        clauseMapper.insert(clause);
        log.info("条款已创建: code={}, title={}", clause.getClauseCode(), clause.getTitle());
        return clause;
    }

    /**
     * 更新条款（版本+1）/ Update clause (increment version)
     */
    @Transactional(rollbackFor = Exception.class)
    public ContractClause update(Long clauseId, ContractClause update) {
        ContractClause existing = clauseMapper.selectById(clauseId);
        if (existing == null) {
            throw new IllegalArgumentException("条款不存在 / Clause not found: " + clauseId);
        }
        existing.setTitle(update.getTitle());
        existing.setContent(update.getContent());
        existing.setCategory(update.getCategory());
        existing.setVersion(existing.getVersion() + 1);
        existing.setEffectiveDate(LocalDate.now());
        clauseMapper.updateById(existing);
        log.info("条款已更新: clauseId={}, version={}", clauseId, existing.getVersion());
        return existing;
    }

    /**
     * 按分类查询条款 / Query clauses by category
     */
    public Page<ContractClause> listByCategory(String category, int page, int size) {
        LambdaQueryWrapper<ContractClause> wrapper = new LambdaQueryWrapper<>();
        if (category != null && !category.isBlank()) {
            wrapper.eq(ContractClause::getCategory, category);
        }
        wrapper.eq(ContractClause::getStatus, "ACTIVE")
               .orderByAsc(ContractClause::getClauseCode)
               .orderByDesc(ContractClause::getVersion);
        return clauseMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 查询某编码的最新版本 / Query latest version by code
     */
    public ContractClause getLatestByCode(String clauseCode) {
        LambdaQueryWrapper<ContractClause> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ContractClause::getClauseCode, clauseCode)
               .eq(ContractClause::getStatus, "ACTIVE")
               .orderByDesc(ContractClause::getVersion)
               .last("LIMIT 1");
        return clauseMapper.selectOne(wrapper);
    }

    /**
     * 按ID查条款 / Query clause by ID
     */
    public ContractClause getById(Long clauseId) {
        return clauseMapper.selectById(clauseId);
    }

    /**
     * 归档条款 / Archive clause
     */
    @Transactional(rollbackFor = Exception.class)
    public void archive(Long clauseId) {
        ContractClause clause = clauseMapper.selectById(clauseId);
        if (clause != null) {
            clause.setStatus("ARCHIVED");
            clauseMapper.updateById(clause);
            log.info("条款已归档: clauseId={}", clauseId);
        }
    }
}
