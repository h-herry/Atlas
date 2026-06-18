package com.atlas.supplier.mapper;

import com.atlas.supplier.entity.SupplierScorecardItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 评分卡明细项 Mapper — 对应 supplier_scorecard_item 表 /
 * Scorecard line item Mapper — maps to supplier_scorecard_item table
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface SupplierScorecardItemMapper extends BaseMapper<SupplierScorecardItem> {
}
