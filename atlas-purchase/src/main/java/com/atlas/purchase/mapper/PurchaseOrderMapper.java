package com.atlas.purchase.mapper;

import com.atlas.purchase.entity.PurchaseOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 采购订单主表 Mapper / Purchase order master table Mapper
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Mapper
public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {

    /**
     * 按幂等请求ID统计已有订单数（幂等校验） / Count existing orders by idempotent request ID
     *
     * @param requestId 请求幂等ID / Idempotent request ID
     * @return 匹配的订单数量 / Number of matching orders
     */
    @Select("SELECT COUNT(*) FROM purchase_order WHERE request_id = #{requestId}")
    int countByRequestId(@Param("requestId") String requestId);

    /**
     * 按幂等请求ID查询已有订单 / Query existing order by idempotent request ID
     *
     * @param requestId 请求幂等ID / Idempotent request ID
     * @return 已存在的订单 / Existing order, null if not found
     */
    @Select("SELECT * FROM purchase_order WHERE request_id = #{requestId} LIMIT 1")
    PurchaseOrder selectByRequestId(@Param("requestId") String requestId);
}
