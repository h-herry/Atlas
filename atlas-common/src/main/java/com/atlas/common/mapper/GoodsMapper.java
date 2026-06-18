package com.atlas.common.mapper;

import com.atlas.common.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品 Mapper / Goods Mapper
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface GoodsMapper extends BaseMapper<Goods> {
}
