package com.atlas.supplier.mapper;

import com.atlas.supplier.entity.BomItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * BOM 明细 Mapper / BOM line item Mapper
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface BomItemMapper extends BaseMapper<BomItem> {
}
