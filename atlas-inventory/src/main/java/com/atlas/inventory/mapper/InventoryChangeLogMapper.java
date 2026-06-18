package com.atlas.inventory.mapper;

import com.atlas.inventory.entity.InventoryChangeLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 库存变动流水 Mapper / Inventory change log Mapper
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Mapper
public interface InventoryChangeLogMapper extends BaseMapper<InventoryChangeLog> {
}
