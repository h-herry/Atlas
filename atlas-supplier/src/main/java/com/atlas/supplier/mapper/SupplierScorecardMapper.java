package com.atlas.supplier.mapper;

import com.atlas.supplier.entity.SupplierScorecard;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商绩效评分卡 Mapper — 对应 supplier_scorecard 表 /
 * Supplier performance scorecard Mapper — maps to supplier_scorecard table
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface SupplierScorecardMapper extends BaseMapper<SupplierScorecard> {
}
