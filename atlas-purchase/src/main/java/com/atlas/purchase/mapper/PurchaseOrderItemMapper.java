package com.atlas.purchase.mapper;

import com.atlas.purchase.entity.PurchaseOrderItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 采购明细表 Mapper / Purchase order item table Mapper
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Mapper
public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {
}
