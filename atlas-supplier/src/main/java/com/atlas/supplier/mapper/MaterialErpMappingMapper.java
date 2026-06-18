package com.atlas.supplier.mapper;

import com.atlas.supplier.entity.MaterialErpMapping;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ERP 物料编码映射 Mapper — 对应 material_erp_mapping 表 /
 * ERP material code mapping Mapper — maps to material_erp_mapping table
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface MaterialErpMappingMapper extends BaseMapper<MaterialErpMapping> {
}
