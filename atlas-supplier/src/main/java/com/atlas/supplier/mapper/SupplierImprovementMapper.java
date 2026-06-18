package com.atlas.supplier.mapper;

import com.atlas.supplier.entity.SupplierImprovement;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商改善跟踪闭环 Mapper — 对应 supplier_improvement 表 /
 * Supplier improvement Mapper — maps to supplier_improvement table
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface SupplierImprovementMapper extends BaseMapper<SupplierImprovement> {
}
