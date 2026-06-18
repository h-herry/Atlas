package com.atlas.supplier.mapper;

import com.atlas.supplier.entity.SupplierClassification;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商分级 Mapper — 对应 supplier_classification 表 /
 * Supplier classification Mapper — maps to supplier_classification table
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface SupplierClassificationMapper extends BaseMapper<SupplierClassification> {
}
