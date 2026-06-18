package com.atlas.common.export.service;

import com.atlas.common.export.entity.ExportTemplate;
import com.atlas.common.export.mapper.ExportTemplateMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 数据导出 Service — 按预设模板导出 Excel，支持导出历史查询 /
 * Data export Service — export Excel via preset templates; supports export history query
 *
 * @since 1.2.22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExportService {

    private final ExportTemplateMapper exportTemplateMapper;

    /**
     * 创建导出模板 / Create export template
     */
    @Transactional(rollbackFor = Exception.class)
    public ExportTemplate createTemplate(ExportTemplate template) {
        exportTemplateMapper.insert(template);
        log.info("导出模板已创建: name={}, module={}", template.getName(), template.getModule());
        return template;
    }

    /**
     * 按模块查询模板 / Query templates by module
     */
    public List<ExportTemplate> listTemplates(String module) {
        LambdaQueryWrapper<ExportTemplate> wrapper = new LambdaQueryWrapper<>();
        if (module != null && !module.isBlank()) {
            wrapper.eq(ExportTemplate::getModule, module);
        }
        wrapper.orderByDesc(ExportTemplate::getCreatedAt);
        return exportTemplateMapper.selectList(wrapper);
    }

    /**
     * 按ID查模板 / Query template by ID
     */
    public ExportTemplate getTemplateById(Long templateId) {
        return exportTemplateMapper.selectById(templateId);
    }

    /**
     * 删除模板 / Delete template
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteTemplate(Long templateId) {
        exportTemplateMapper.deleteById(templateId);
        log.info("导出模板已删除: templateId={}", templateId);
    }

    /**
     * 按模板导出数据（占位实现 — 实际导出逻辑由具体模块实现）/ Export by template (placeholder — actual export logic per module)
     */
    public String exportByTemplate(Long templateId) {
        ExportTemplate template = exportTemplateMapper.selectById(templateId);
        if (template == null) {
            throw new IllegalArgumentException("模板不存在 / Template not found: " + templateId);
        }
        log.info("按模板导出: templateId={}, module={}", templateId, template.getModule());
        // TODO: 实际 Excel 生成逻辑由各模块实现 / Actual Excel generation delegated to individual modules
        return "export_" + templateId + "_" + System.currentTimeMillis() + ".xlsx";
    }
}
