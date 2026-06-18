package com.atlas.supplier.service;

import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.supplier.entity.MaterialCategory;
import com.atlas.supplier.mapper.MaterialCategoryNewMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 物料分类 Service — 树形查询 / 分类移动 / 编码自动生成 /
 * Material category Service — tree query / category move / auto code generation
 *
 * <p>支持 4 级树形结构，编码规则：大类 2 位 + 中类 2 位 + 小类 2 位 + 细类 2 位，同层自增。 /
 * Supports 4-level tree structure, coding rule: category 2-digit + sub-category 2-digit + class 2-digit + sub-class 2-digit.</p>
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialCategoryService {

    private final MaterialCategoryNewMapper materialCategoryNewMapper;

    /** 编码长度 / Code segment length */
    private static final int CODE_SEGMENT_LENGTH = 2;

    // ==================== 树形查询 / Tree Query ====================

    /**
     * 查询所有分类（全量树） / Query all categories (full tree)
     *
     * @return 树形列表（按 parentId 分组构建） / Tree list (grouped by parentId)
     */
    public List<MaterialCategory> listAll() {
        List<MaterialCategory> all = materialCategoryNewMapper.selectList(
            new LambdaQueryWrapper<MaterialCategory>()
                .eq(MaterialCategory::getStatus, 1)
                .orderByAsc(MaterialCategory::getLevel, MaterialCategory::getSortOrder));
        return buildTree(all, 0L);
    }

    /**
     * 按层级查询 / Query by level
     *
     * @param level 层级: 1大类 2中类 3小类 4细类 / Level: 1-category, 2-subcategory, 3-class, 4-subclass
     * @return 该层级所有有效分类 / All active categories at this level
     */
    public List<MaterialCategory> listByLevel(Integer level) {
        return materialCategoryNewMapper.selectList(
            new LambdaQueryWrapper<MaterialCategory>()
                .eq(MaterialCategory::getLevel, level)
                .eq(MaterialCategory::getStatus, 1)
                .orderByAsc(MaterialCategory::getSortOrder));
    }

    /**
     * 查询子分类（单层） / Query direct children (single level)
     *
     * @param parentId 父分类ID / Parent category ID
     * @return 子分类列表 / Children list
     */
    public List<MaterialCategory> listChildren(Long parentId) {
        return materialCategoryNewMapper.selectList(
            new LambdaQueryWrapper<MaterialCategory>()
                .eq(MaterialCategory::getParentId, parentId)
                .eq(MaterialCategory::getStatus, 1)
                .orderByAsc(MaterialCategory::getSortOrder));
    }

    /**
     * 查询指定分类的信息 / Query category by ID
     */
    public MaterialCategory getById(Long id) {
        MaterialCategory category = materialCategoryNewMapper.selectById(id);
        if (category == null) {
            throw new BizException(ErrorCode.DATA_NOT_FOUND, "物料分类不存在: " + id);
        }
        return category;
    }

    // ==================== 分类移动 / Category Move ====================

    /**
     * 新增分类（自动生成编码） / Create category (auto-generate code)
     *
     * @param category 分类对象（需含 parentId / name / level） / Category object (must include parentId/name/level)
     */
    @Transactional(rollbackFor = Exception.class)
    public MaterialCategory create(MaterialCategory category) {
        // 生成编码 / Generate code
        String parentCode = "";
        if (category.getParentId() != null && category.getParentId() > 0) {
            MaterialCategory parent = getById(category.getParentId());
            parentCode = parent.getCode();
        }
        category.setCode(generateCode(parentCode, category.getLevel()));
        category.setStatus(1);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        materialCategoryNewMapper.insert(category);
        log.info("创建物料分类: code={}, name={}, level={}", category.getCode(), category.getName(), category.getLevel());
        return category;
    }

    /**
     * 更新分类 / Update category
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(MaterialCategory category) {
        MaterialCategory existing = getById(category.getId());
        category.setUpdatedAt(LocalDateTime.now());
        materialCategoryNewMapper.updateById(category);
        log.info("更新物料分类: id={}, name={}", category.getId(), category.getName());
    }

    /**
     * 分类移动（变更父节点） / Move category (change parent)
     *
     * <p>移动后子节点层级不变，但需要确保新父节点层级低于当前节点层级。 /
     * After moving, child level remains unchanged; new parent level must be lower.</p>
     *
     * @param categoryId    待移动的分类ID / Category ID to move
     * @param newParentId   新父节点ID / New parent ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void move(Long categoryId, Long newParentId) {
        MaterialCategory category = getById(categoryId);
        // 校验：父节点层级必须低于当前节点 / Validate: parent level must be lower
        if (newParentId > 0) {
            MaterialCategory newParent = getById(newParentId);
            if (newParent.getLevel() >= category.getLevel()) {
                throw new BizException(ErrorCode.PARAM_INVALID,
                    "父节点层级必须低于当前节点 / Parent level must be lower than current node: "
                    + newParent.getLevel() + " >= " + category.getLevel());
            }
            // 重新生成编码 / Re-generate code
            category.setCode(generateCode(newParent.getCode(), category.getLevel()));
        }
        category.setParentId(newParentId);
        category.setUpdatedAt(LocalDateTime.now());
        materialCategoryNewMapper.updateById(category);
        log.info("移动物料分类: id={}, newParentId={}", categoryId, newParentId);
    }

    /**
     * 停用分类 / Deactivate category
     */
    @Transactional(rollbackFor = Exception.class)
    public void deactivate(Long id) {
        materialCategoryNewMapper.update(null,
            new LambdaUpdateWrapper<MaterialCategory>()
                .eq(MaterialCategory::getId, id)
                .set(MaterialCategory::getStatus, 0)
                .set(MaterialCategory::getUpdatedAt, LocalDateTime.now()));
        log.info("停用物料分类: id={}", id);
    }

    // ==================== 编码生成 / Code Generation ====================

    /**
     * 自动生成分类编码 / Auto-generate category code
     *
     * <p>编码规则：父编码 + 2位自增序号 / Rule: parentCode + 2-digit auto-increment</p>
     *
     * @param parentCode 父编码 / Parent code
     * @param level      层级 / Level
     * @return 新编码 / New code
     */
    private String generateCode(String parentCode, Integer level) {
        // 查询同层级最大编码 / Find max code at same level
        String prefix = parentCode.isEmpty() ? "" : parentCode;
        List<MaterialCategory> siblings = materialCategoryNewMapper.selectList(
            new LambdaQueryWrapper<MaterialCategory>()
                .eq(MaterialCategory::getLevel, level)
                .likeRight(prefix.isEmpty() ? null : MaterialCategory::getCode, prefix)
                .orderByDesc(MaterialCategory::getCode)
                .last("LIMIT 1"));

        int nextSeq = 1;
        if (!siblings.isEmpty()) {
            String maxCode = siblings.get(0).getCode();
            try {
                String seqStr = maxCode.substring(prefix.length());
                nextSeq = Integer.parseInt(seqStr) + 1;
            } catch (Exception e) {
                log.warn("解析编码异常 / Code parse error: {}", maxCode);
            }
        }
        return prefix + String.format("%0" + CODE_SEGMENT_LENGTH + "d", nextSeq);
    }

    // ==================== 树构建 / Tree Builder ====================

    /**
     * 递归构建树 / Recursive tree builder
     */
    private List<MaterialCategory> buildTree(List<MaterialCategory> all, Long parentId) {
        Map<Long, List<MaterialCategory>> grouped = all.stream()
            .collect(Collectors.groupingBy(c -> c.getParentId() != null ? c.getParentId() : 0L));
        return grouped.getOrDefault(parentId, new ArrayList<>());
    }
}
