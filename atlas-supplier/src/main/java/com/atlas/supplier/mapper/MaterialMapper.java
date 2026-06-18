package com.atlas.supplier.mapper;

import com.atlas.common.entity.Goods;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 物料 Mapper（复用 goods 表，作为物料主数据） / Material Mapper (reuses goods as material master)
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface MaterialMapper extends BaseMapper<Goods> {
}
