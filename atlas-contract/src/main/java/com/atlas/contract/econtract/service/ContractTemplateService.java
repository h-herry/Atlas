package com.atlas.contract.econtract.service;

import com.atlas.contract.econtract.dto.TemplateDTO;
import com.atlas.contract.econtract.mapper.CntTemplateMapper;
import com.atlas.contract.econtract.model.CntTemplate;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 合同模板服务 — 模板CRUD + 基于模板生成合同 /
 * Contract template service — template CRUD + generate contract from template
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractTemplateService extends ServiceImpl<CntTemplateMapper, CntTemplate> {

    private final CntTemplateMapper templateMapper;

    // ============ 模板CRUD / Template CRUD ============

    /**
     * 分页查询模板列表（支持分类筛选） /
     * Paginated template list (with category filter support)
     */
    public Page<CntTemplate> page(String category, String keyword, int page, int size) {
        LambdaQueryWrapper<CntTemplate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(CntTemplate::getCategory, category);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(CntTemplate::getTemplateName, keyword)
                   .or().like(CntTemplate::getTemplateCode, keyword);
        }
        wrapper.orderByDesc(CntTemplate::getUpdatedAt);
        return templateMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 获取所有启用模板列表（不分页） /
     * Get all active template list (no pagination)
     */
    public java.util.List<CntTemplate> listActive() {
        LambdaQueryWrapper<CntTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CntTemplate::getIsActive, 1)
               .orderByDesc(CntTemplate::getUpdatedAt);
        return templateMapper.selectList(wrapper);
    }

    /**
     * 根据编码查询模板 / Query template by code
     */
    public CntTemplate getByCode(String templateCode) {
        LambdaQueryWrapper<CntTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CntTemplate::getTemplateCode, templateCode);
        return templateMapper.selectOne(wrapper);
    }

    /**
     * 创建合同模板 / Create contract template
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean create(TemplateDTO dto) {
        // 编码唯一性校验 / Code uniqueness validation
        CntTemplate existing = getByCode(dto.getTemplateCode());
        if (existing != null) {
            throw new IllegalArgumentException("模板编码已存在: " + dto.getTemplateCode());
        }

        CntTemplate template = new CntTemplate();
        template.setTemplateName(dto.getTemplateName());
        template.setTemplateCode(dto.getTemplateCode());
        template.setCategory(dto.getCategory());
        template.setDescription(dto.getDescription());
        template.setFilePath(dto.getFilePath());
        template.setVersion(dto.getVersion() != null ? dto.getVersion() : "1.0");
        template.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : 1);
        template.setCreatedBy(dto.getCreatedBy());
        return save(template);
    }

    /**
     * 更新合同模板 / Update contract template
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean updateTemplate(Long id, TemplateDTO dto) {
        CntTemplate template = getById(id);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在: " + id);
        }

        // 编码变更时校验唯一性 / Validate uniqueness when code changes
        if (!template.getTemplateCode().equals(dto.getTemplateCode())) {
            CntTemplate dup = getByCode(dto.getTemplateCode());
            if (dup != null) {
                throw new IllegalArgumentException("模板编码已存在: " + dto.getTemplateCode());
            }
        }

        template.setTemplateName(dto.getTemplateName());
        template.setTemplateCode(dto.getTemplateCode());
        template.setCategory(dto.getCategory());
        template.setDescription(dto.getDescription());
        template.setFilePath(dto.getFilePath());
        if (dto.getVersion() != null) template.setVersion(dto.getVersion());
        if (dto.getIsActive() != null) template.setIsActive(dto.getIsActive());
        return updateById(template);
    }

    /**
     * 删除模板（物理删除） / Delete template (physical deletion)
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteTemplate(Long id) {
        CntTemplate template = getById(id);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在: " + id);
        }
        return removeById(id);
    }

    /**
     * 基于模板生成合同 — 返回模板信息供下游使用 /
     * Generate contract from template — returns template info for downstream use
     */
    public CntTemplate generateFromTemplate(Long templateId) {
        CntTemplate template = getById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在: " + templateId);
        }
        if (template.getIsActive() != 1) {
            throw new IllegalStateException("模板未启用: " + template.getTemplateName());
        }
        log.info("基于模板 [{}] 生成合同", template.getTemplateCode());
        return template;
    }
}
