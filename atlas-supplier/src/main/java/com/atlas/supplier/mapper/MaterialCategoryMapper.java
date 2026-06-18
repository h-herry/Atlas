package com.atlas.supplier.mapper;

import com.atlas.common.entity.GoodsCategory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物料分类 Mapper（复用 goods_category 表） / Material category Mapper (reuses goods_category)
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface MaterialCategoryMapper extends BaseMapper<GoodsCategory> {
}
