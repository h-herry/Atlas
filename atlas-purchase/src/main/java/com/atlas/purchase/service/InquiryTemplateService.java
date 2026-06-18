package com.atlas.purchase.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.purchase.entity.InquiryTemplate;
import com.atlas.purchase.mapper.InquiryTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 询价模板 Service — CRUD / 基于模板快速创建询价单 /
 * Inquiry template Service — CRUD / quick inquiry creation from template
 *
 * <p>询价模板关联物料分类和属性字段，发货前预填写公共信息，
 * 减少重复录入，提升询价效率。 /
 * RFQ templates linked to material categories and attribute fields,
 * pre-fill common information before sending, reducing repetitive data entry.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryTemplateService {

    private final InquiryTemplateMapper inquiryTemplateMapper;

    // ==================== CRUD ====================

    /**
     * 创建询价模板 / Create inquiry template
     *
     * @param template 模板对象 / Template object
     * @return 创建后的模板 / Created template
     */
    @Transactional(rollbackFor = Exception.class)
    public InquiryTemplate create(InquiryTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setStatus(template.getStatus() != null ? template.getStatus() : 1);
        inquiryTemplateMapper.insert(template);
        log.info("创建询价模板: name={}, templateId={}", template.getName(), template.getTemplateId());
        return template;
    }

    /**
     * 更新询价模板 / Update inquiry template
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(InquiryTemplate template) {
        InquiryTemplate existing = inquiryTemplateMapper.selectById(template.getId());
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "询价模板不存在: " + template.getId());
        }
        template.setUpdatedAt(LocalDateTime.now());
        inquiryTemplateMapper.updateById(template);
        log.info("更新询价模板: id={}, name={}", template.getId(), template.getName());
    }

    /**
     * 删除询价模板 / Delete inquiry template
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        inquiryTemplateMapper.deleteById(id);
        log.info("删除询价模板: id={}", id);
    }

    /**
     * 查询询价模板详情 / Query inquiry template detail
     */
    public InquiryTemplate getById(Long id) {
        InquiryTemplate template = inquiryTemplateMapper.selectById(id);
        if (template == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "询价模板不存在: " + id);
        }
        return template;
    }

    /**
     * 分页查询所有有效模板 / Paginated query of all active templates
     *
     * @param page 分页对象 / Page object
     * @return 分页结果 / Paginated results
     */
    public IPage<InquiryTemplate> page(IPage<InquiryTemplate> page) {
        LambdaQueryWrapper<InquiryTemplate> wrapper = new LambdaQueryWrapper<InquiryTemplate>()
            .eq(InquiryTemplate::getStatus, 1)
            .orderByDesc(InquiryTemplate::getCreatedAt);
        return inquiryTemplateMapper.selectPage(page, wrapper);
    }

    /**
     * 按分类ID查询模板 / Query templates by category ID
     *
     * @param categoryId 物料分类ID / Material category ID
     * @return 模板列表 / Template list
     */
    public java.util.List<InquiryTemplate> listByCategoryId(Long categoryId) {
        return inquiryTemplateMapper.selectList(
            new LambdaQueryWrapper<InquiryTemplate>()
                .eq(InquiryTemplate::getCategoryId, categoryId)
                .eq(InquiryTemplate::getStatus, 1)
                .orderByDesc(InquiryTemplate::getCreatedAt));
    }

    // ==================== 基于模板创建询价单 / Create Inquiry from Template ====================

    /**
     * 基于模板创建询价单 / Create inquiry from template
     *
     * <p>根据模板配置自动填充交期要求、质量资质要求、价格明细开关等字段。 /
     * Auto-fills delivery requirement, quality cert requirement, price breakdown toggle per template config.</p>
     *
     * @param templateId 模板ID / Template ID
     * @return 模板配置信息（供调用方构建询价单） / Template config (for caller to build inquiry)
     */
    public InquiryTemplate createInquiryFromTemplate(Long templateId) {
        InquiryTemplate template = getById(templateId);
        if (template.getStatus() != 1) {
            throw new BizException(ErrorCode.ILLEGAL_STATE, "模板已停用: " + templateId);
        }
        log.info("基于模板创建询价单: templateId={}, name={}", templateId, template.getName());
        return template;
    }
}
