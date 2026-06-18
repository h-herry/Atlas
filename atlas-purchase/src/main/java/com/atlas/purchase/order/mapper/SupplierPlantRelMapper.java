package com.atlas.purchase.order.mapper;

import com.atlas.purchase.order.entity.SupplierPlantRel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 供应商-工厂关联 Mapper / Supplier-Plant relation Mapper
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Mapper
public interface SupplierPlantRelMapper extends BaseMapper<SupplierPlantRel> {
}
