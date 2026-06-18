package com.atlas.supplier.mapper;

import com.atlas.supplier.entity.MaterialCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物料分类 Mapper — 对应 material_category 表（4级树形结构） /
 * Material category Mapper — maps to material_category table (4-level tree)
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface MaterialCategoryNewMapper extends BaseMapper<MaterialCategory> {
}
