package com.atlas.common.template.service;

import com.atlas.common.template.entity.ApprovalTemplate;
import com.atlas.common.template.mapper.ApprovalTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 审批流模板 Service — 预置审批链模板，支持 CRUD + 按模块查询 + 默认模板 /
 * Approval flow template Service — preset approval chain templates; supports CRUD + module query + default template
 *
 * <p>预置场景: 供应商准入 / 订单变更 / 合同签署 / 付款审批 /
 * Preset scenarios: supplier onboarding / order change / contract signing / payment approval</p>
 *
 * @since 1.2.22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApprovalTemplateService {

    private final ApprovalTemplateMapper templateMapper;

    /**
     * 创建模板 / Create template
     */
    @Transactional(rollbackFor = Exception.class)
    public ApprovalTemplate create(ApprovalTemplate template) {
        template.setStatus("ACTIVE");
        template.setIsDefault(template.getIsDefault() != null ? template.getIsDefault() : 0);
        templateMapper.insert(template);
        log.info("审批模板已创建: name={}, module={}", template.getName(), template.getModule());
        return template;
    }

    /**
     * 更新模板 / Update template
     */
    @Transactional(rollbackFor = Exception.class)
    public ApprovalTemplate update(Long templateId, ApprovalTemplate update) {
        ApprovalTemplate existing = templateMapper.selectById(templateId);
        if (existing == null) {
            throw new IllegalArgumentException("模板不存在 / Template not found: " + templateId);
        }
        existing.setName(update.getName());
        existing.setSteps(update.getSteps());
        existing.setIsDefault(update.getIsDefault());
        existing.setStatus(update.getStatus());
        templateMapper.updateById(existing);
        log.info("审批模板已更新: templateId={}", templateId);
        return existing;
    }

    /**
     * 按模块查询模板 / Query templates by module
     */
    public List<ApprovalTemplate> listByModule(String module) {
        LambdaQueryWrapper<ApprovalTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalTemplate::getModule, module)
               .eq(ApprovalTemplate::getStatus, "ACTIVE")
               .orderByDesc(ApprovalTemplate::getIsDefault);
        return templateMapper.selectList(wrapper);
    }

    /**
     * 获取模块默认模板 / Get default template for module
     */
    public ApprovalTemplate getDefault(String module) {
        LambdaQueryWrapper<ApprovalTemplate> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApprovalTemplate::getModule, module)
               .eq(ApprovalTemplate::getIsDefault, 1)
               .eq(ApprovalTemplate::getStatus, "ACTIVE")
               .last("LIMIT 1");
        return templateMapper.selectOne(wrapper);
    }

    /**
     * 按ID查模板 / Query template by ID
     */
    public ApprovalTemplate getById(Long templateId) {
        return templateMapper.selectById(templateId);
    }

    /**
     * 删除模板 / Delete template
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long templateId) {
        templateMapper.deleteById(templateId);
        log.info("审批模板已删除: templateId={}", templateId);
    }
}
