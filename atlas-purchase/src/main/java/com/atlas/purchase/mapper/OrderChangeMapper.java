package com.atlas.purchase.mapper;

import com.atlas.purchase.entity.OrderChange;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单变更管理 Mapper / Order change (ECN) Mapper
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface OrderChangeMapper extends BaseMapper<OrderChange> {
}
