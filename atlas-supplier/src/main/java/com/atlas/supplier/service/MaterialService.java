package com.atlas.supplier.service;

import cn.hutool.core.util.StrUtil;
import com.atlas.common.core.enums.ErrorCode;
import com.atlas.common.core.exception.BizException;
import com.atlas.common.entity.Goods;
import com.atlas.common.entity.GoodsCategory;
import com.atlas.supplier.mapper.MaterialCategoryMapper;
import com.atlas.supplier.mapper.MaterialMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 物料主数据 Service / Material master data Service
 *
 * <p>提供物料 CRUD、分页查询、物料编码自动生成（分类前缀+序号）。 /
 * Provides material CRUD, paginated queries, and auto material code generation (category prefix + sequence).
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MaterialService {

    private final MaterialMapper materialMapper;
    private final MaterialCategoryMapper categoryMapper;

    // ==================== 物料管理 / Material Management ====================

    /**
     * 新增物料（自动生成物料编码：分类前缀 + 8位序号） /
     * Create material (auto-generate code: category prefix + 8-digit sequence)
     *
     * @param material 物料信息 / Material info
     * @return 保存后的物料 / Saved material
     */
    @Transactional(rollbackFor = Exception.class)
    public Goods save(Goods material) {
        // 生成物料编码 / Generate material code
        if (StrUtil.isBlank(material.getGoodsCode())) {
            material.setGoodsCode(generateMaterialCode(material.getCategoryId()));
        }
        // 编码唯一性校验 / Code uniqueness check
        Long count = materialMapper.selectCount(
                new LambdaQueryWrapper<Goods>().eq(Goods::getGoodsCode, material.getGoodsCode()));
        if (count > 0) {
            throw new BizException(ErrorCode.MATERIAL_CODE_DUPLICATE);
        }
        material.setStatus(material.getStatus() != null ? material.getStatus() : 1);
        material.setCreatedAt(LocalDateTime.now());
        material.setUpdatedAt(LocalDateTime.now());
        materialMapper.insert(material);
        log.info("物料创建成功: code={} name={}", material.getGoodsCode(), material.getGoodsName());
        return material;
    }

    /**
     * 更新物料 / Update material
     */
    @Transactional(rollbackFor = Exception.class)
    public Goods update(Goods material) {
        Goods existing = materialMapper.selectById(material.getId());
        if (existing == null) {
            throw new BizException(ErrorCode.MATERIAL_NOT_EXIST);
        }
        material.setUpdatedAt(LocalDateTime.now());
        materialMapper.updateById(material);
        log.info("物料更新成功: id={} name={}", material.getId(), material.getGoodsName());
        return materialMapper.selectById(material.getId());
    }

    /**
     * 按ID查询物料 / Query material by ID
     */
    public Goods getById(Long id) {
        Goods material = materialMapper.selectById(id);
        if (material == null) {
            throw new BizException(ErrorCode.MATERIAL_NOT_EXIST);
        }
        return material;
    }

    /**
     * 按物料编码查询 / Query material by code
     */
    public Goods getByCode(String code) {
        Goods material = materialMapper.selectOne(
                new LambdaQueryWrapper<Goods>().eq(Goods::getGoodsCode, code));
        if (material == null) {
            throw new BizException(ErrorCode.MATERIAL_NOT_EXIST);
        }
        return material;
    }

    /**
     * 分页查询物料（支持按编码/名称/分类/类型筛选） /
     * Paginated material query (supports filter by code/name/category/type)
     *
     * @param keyword      关键字（匹配编码或名称） / Keyword (matches code or name)
     * @param categoryId   分类ID（可选） / Category ID (optional)
     * @param materialType 物料类型（可选） / Material type (optional)
     * @param status       状态（可选） / Status (optional)
     * @param page         当前页 / Current page
     * @param size         每页大小 / Page size
     * @return 分页结果 / Paginated result
     */
    public Page<Goods> page(String keyword, Long categoryId, String materialType, Integer status, int page, int size) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.and(w -> w.like(Goods::getGoodsCode, keyword).or().like(Goods::getGoodsName, keyword));
        }
        if (categoryId != null) {
            wrapper.eq(Goods::getCategoryId, categoryId);
        }
        // Goods 实体暂无 materialType 字段，该条件留待扩展 goods 表后启用 / Goods entity has no materialType field yet; condition reserved for future extension
        if (status != null) {
            wrapper.eq(Goods::getStatus, status);
        }
        wrapper.orderByDesc(Goods::getCreatedAt);
        return materialMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 启用/禁用物料 / Enable/disable material
     */
    @CacheEvict(value = "material", key = "#id")
    @Transactional(rollbackFor = Exception.class)
    public void toggleStatus(Long id, Integer status) {
        Goods material = getById(id);
        material.setStatus(status);
        material.setUpdatedAt(LocalDateTime.now());
        materialMapper.updateById(material);
        log.info("物料状态变更: id={} status={}", id, status);
    }

    // ==================== 物料分类 / Material Category ====================

    /**
     * 新增物料分类 / Create material category
     */
    @Transactional(rollbackFor = Exception.class)
    public GoodsCategory saveCategory(GoodsCategory category) {
        Long count = categoryMapper.selectCount(
                new LambdaQueryWrapper<GoodsCategory>().eq(GoodsCategory::getCategoryCode, category.getCategoryCode()));
        if (count > 0) {
            throw new BizException(ErrorCode.CATEGORY_CODE_DUPLICATE);
        }
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());
        categoryMapper.insert(category);
        log.info("物料分类创建成功: code={} name={}", category.getCategoryCode(), category.getCategoryName());
        return category;
    }

    /**
     * 分页查询物料分类 / Paginated query of material categories
     */
    public Page<GoodsCategory> pageCategory(String keyword, Long parentId, int page, int size) {
        LambdaQueryWrapper<GoodsCategory> wrapper = new LambdaQueryWrapper<>();
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(GoodsCategory::getCategoryName, keyword);
        }
        if (parentId != null) {
            wrapper.eq(GoodsCategory::getParentId, parentId);
        }
        wrapper.orderByAsc(GoodsCategory::getSortOrder);
        return categoryMapper.selectPage(new Page<>(page, size), wrapper);
    }

    // ==================== 私有方法 / Private Methods ====================

    /**
     * 生成物料编码：分类前缀 + 8位自增序号 / Generate material code: category prefix + 8-digit auto-increment
     *
     * <p>规则：若 categoryId 存在则取其 category_code 前2位作为前缀，
     * 否则使用默认前缀 "MT"。 /
     * Rule: if categoryId exists, use first 2 chars of category_code as prefix,
     * otherwise default to "MT".
     */
    private String generateMaterialCode(Long categoryId) {
        String prefix = "MT";
        if (categoryId != null) {
            GoodsCategory category = categoryMapper.selectById(categoryId);
            if (category != null && StrUtil.isNotBlank(category.getCategoryCode())) {
                prefix = category.getCategoryCode().length() >= 2
                        ? category.getCategoryCode().substring(0, 2).toUpperCase()
                        : category.getCategoryCode().toUpperCase();
            }
        }
        // 取当前最大序号 + 1 / Get max sequence + 1
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Goods::getGoodsCode, prefix)
                .orderByDesc(Goods::getGoodsCode)
                .last("LIMIT 1");
        Goods latest = materialMapper.selectOne(wrapper);
        int seq = 1;
        if (latest != null && latest.getGoodsCode().length() >= prefix.length() + 1) {
            try {
                seq = Integer.parseInt(latest.getGoodsCode().substring(prefix.length())) + 1;
            } catch (NumberFormatException e) {
                seq = 1;
            }
        }
        return prefix + String.format("%08d", seq);
    }
}
