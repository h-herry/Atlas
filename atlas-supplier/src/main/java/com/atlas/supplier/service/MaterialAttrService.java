package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.MaterialAttrTemplate;
import com.atlas.supplier.entity.MaterialCategory;
import com.atlas.supplier.mapper.MaterialAttrTemplateMapper;
import com.atlas.supplier.mapper.MaterialCategoryNewMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 物料规格属性模板 Service — 属性模板CRUD / 属性继承 / 完整度校验 /
 * Material attribute template Service — CRUD / attribute inheritance / integrity validation
 *
 * <p>按物料分类定义结构化属性字段，子分类自动继承父分类属性集，
 * 供应商报价时按属性模板填写规格值，系统自动校验必填项和类型。 /
 * Defines structured attributes per material category, sub-categories auto-inherit parent attributes,
 * suppliers fill spec values per attribute template during quotation, system auto-validates required fields and types.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialAttrService {

    private final MaterialAttrTemplateMapper materialAttrTemplateMapper;
    private final MaterialCategoryNewMapper materialCategoryNewMapper;

    // ==================== 属性模板 CRUD ====================

    /**
     * 按分类获取属性模板（含继承） / Get attribute templates by category (with inheritance)
     *
     * <p>子分类继承父分类属性集，返回合并去重后的完整属性列表。 /
     * Sub-categories inherit parent attributes; returns merged, deduplicated full attribute list.</p>
     *
     * @param categoryId 分类ID / Category ID
     * @return 属性模板列表（按 sortOrder 排序） / Attribute template list (sorted by sortOrder)
     */
    public List<MaterialAttrTemplate> getByCategoryId(Long categoryId) {
        // 获取分类链上的所有祖先ID / Get all ancestor IDs in category chain
        Set<Long> categoryIds = getAncestorCategoryIds(categoryId);
        categoryIds.add(categoryId);

        return materialAttrTemplateMapper.selectList(
            new LambdaQueryWrapper<MaterialAttrTemplate>()
                .in(MaterialAttrTemplate::getCategoryId, categoryIds)
                .orderByAsc(MaterialAttrTemplate::getSortOrder));
    }

    /**
     * 新增属性模板 / Create attribute template
     */
    @Transactional(rollbackFor = Exception.class)
    public MaterialAttrTemplate create(MaterialAttrTemplate template) {
        // 校验属性类型 / Validate attribute type
        validateAttrType(template.getAttrType(), template.getEnumValues());
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        materialAttrTemplateMapper.insert(template);
        log.info("创建属性模板: categoryId={}, attrName={}, type={}",
            template.getCategoryId(), template.getAttrName(), template.getAttrType());
        return template;
    }

    /**
     * 更新属性模板 / Update attribute template
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(MaterialAttrTemplate template) {
        MaterialAttrTemplate existing = materialAttrTemplateMapper.selectById(template.getId());
        if (existing == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "属性模板不存在: " + template.getId());
        }
        validateAttrType(template.getAttrType(), template.getEnumValues());
        template.setUpdatedAt(LocalDateTime.now());
        materialAttrTemplateMapper.updateById(template);
        log.info("更新属性模板: id={}, attrName={}", template.getId(), template.getAttrName());
    }

    /**
     * 删除属性模板 / Delete attribute template
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        materialAttrTemplateMapper.deleteById(id);
        log.info("删除属性模板: id={}", id);
    }

    // ==================== 属性完整度校验 / Attribute Integrity Validation ====================

    /**
     * 校验属性完整度 / Validate attribute completeness
     *
     * <p>检查给定的键值对是否满足必填属性且值类型匹配。 /
     * Checks if the given key-value pairs satisfy required attributes and type matching.</p>
     *
     * @param categoryId 分类ID / Category ID
     * @param attrValues 属性键值对 / Attribute key-value pairs (attrName → value)
     * @return 校验未通过的属性名列表（空表示全部通过） / List of attribute names that failed validation
     */
    public List<String> validateAttributes(Long categoryId, Map<String, String> attrValues) {
        List<MaterialAttrTemplate> templates = getByCategoryId(categoryId);
        List<String> errors = new ArrayList<>();

        for (MaterialAttrTemplate tpl : templates) {
            String value = attrValues.get(tpl.getAttrName());
            // 必填校验 / Required check
            if (tpl.getIsRequired() == 1 && (value == null || value.isBlank())) {
                errors.add(tpl.getAttrName() + " (必填)");
                continue;
            }
            if (value == null) continue;

            // 类型校验 / Type check
            switch (tpl.getAttrType()) {
                case "NUMBER":
                    try {
                        Double.parseDouble(value);
                    } catch (NumberFormatException e) {
                        errors.add(tpl.getAttrName() + " (应为数字)");
                    }
                    break;
                case "ENUM":
                    if (tpl.getEnumValues() != null) {
                        Set<String> allowed = Arrays.stream(tpl.getEnumValues().split(","))
                            .map(String::trim).collect(Collectors.toSet());
                        if (!allowed.contains(value)) {
                            errors.add(tpl.getAttrName() + " (应为: " + tpl.getEnumValues() + ")");
                        }
                    }
                    break;
                case "DATE":
                    try {
                        java.time.LocalDate.parse(value);
                    } catch (Exception e) {
                        errors.add(tpl.getAttrName() + " (格式: yyyy-MM-dd)");
                    }
                    break;
                case "STRING":
                default:
                    break; // 字符串无需校验 / String needs no validation
            }
        }
        return errors;
    }

    /**
     * 批量查询分类的属性模板（含继承） / Batch query attribute templates by categories
     */
    public Map<Long, List<MaterialAttrTemplate>> getByCategoryIds(List<Long> categoryIds) {
        Map<Long, List<MaterialAttrTemplate>> result = new LinkedHashMap<>();
        for (Long id : categoryIds) {
            result.put(id, getByCategoryId(id));
        }
        return result;
    }

    // ==================== 内部方法 / Internal ====================

    /**
     * 获取分类的祖先链 / Get ancestor chain of a category
     */
    private Set<Long> getAncestorCategoryIds(Long categoryId) {
        Set<Long> ancestorIds = new HashSet<>();
        Long currentId = categoryId;
        while (currentId != null && currentId > 0) {
            MaterialCategory category = materialCategoryNewMapper.selectById(currentId);
            if (category == null || category.getParentId() == null || category.getParentId() == 0) break;
            ancestorIds.add(category.getParentId());
            currentId = category.getParentId();
        }
        return ancestorIds;
    }

    /**
     * 校验属性类型合法性 / Validate attribute type
     */
    private void validateAttrType(String attrType, String enumValues) {
        Set<String> validTypes = Set.of("STRING", "NUMBER", "ENUM", "DATE");
        if (!validTypes.contains(attrType)) {
            throw new BizException(ErrorCode.PARAM_INVALID, "无效属性类型: " + attrType);
        }
        if ("ENUM".equals(attrType) && (enumValues == null || enumValues.isBlank())) {
            throw new BizException(ErrorCode.PARAM_INVALID, "ENUM 类型必须提供枚举值列表");
        }
    }
}
