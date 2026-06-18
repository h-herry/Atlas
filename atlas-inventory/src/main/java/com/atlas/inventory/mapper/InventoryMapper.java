package com.atlas.inventory.mapper;

import com.atlas.inventory.entity.Inventory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.*;

/**
 * 库存 Mapper — 包含乐观锁扣减/入库的定制 SQL /
 * Inventory Mapper — includes custom SQL for optimistic-lock deduction and inbound
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 乐观锁扣减库存 / Optimistic-lock inventory deduction
     * <p>
     * SQL: UPDATE inventory SET quantity = quantity - #{qty}, version = version + 1
     *      WHERE id = #{id} AND version = #{version}
     *
     * @param id      库存记录ID / Inventory record ID
     * @param qty     扣减数量 / Deduction quantity
     * @param version 当前版本号 / Current version
     * @return 影响行数（0 表示版本冲突） / Affected rows (0 = version conflict)
     */
    @Update("UPDATE inventory SET quantity = quantity - #{qty}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version}")
    int deductStock(@Param("id") Long id,
                    @Param("qty") java.math.BigDecimal qty,
                    @Param("version") Integer version);

    /**
     * 乐观锁增加库存（入库） / Optimistic-lock stock addition (inbound)
     * <p>
     * SQL: UPDATE inventory SET quantity = quantity + #{qty}, version = version + 1
     *      WHERE id = #{id} AND version = #{version}
     *
     * @param id      库存记录ID / Inventory record ID
     * @param qty     入库数量 / Inbound quantity
     * @param version 当前版本号 / Current version
     * @return 影响行数（0 表示版本冲突） / Affected rows (0 = version conflict)
     */
    @Update("UPDATE inventory SET quantity = quantity + #{qty}, version = version + 1 " +
            "WHERE id = #{id} AND version = #{version}")
    int addStock(@Param("id") Long id,
                 @Param("qty") java.math.BigDecimal qty,
                 @Param("version") Integer version);

    /**
     * 按 SKU ID 和仓库ID 查询库存 / Query inventory by SKU ID and warehouse ID
     *
     * @param skuId       SKU ID
     * @param warehouseId 仓库ID / Warehouse ID
     * @return 库存记录 / Inventory record
     */
    @Select("SELECT * FROM inventory WHERE sku_id = #{skuId} AND warehouse_id = #{warehouseId} LIMIT 1")
    Inventory selectBySkuAndWarehouse(@Param("skuId") Long skuId,
                                      @Param("warehouseId") Long warehouseId);
}
