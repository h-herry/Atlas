package com.atlas.receipt.settlement.mapper;

import com.atlas.receipt.settlement.entity.SettlementItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 结算明细 Mapper / Settlement item Mapper
 *
 * @author Atlas Team
 * @since 1.2.502
 */
@Mapper
public interface SettlementItemMapper extends BaseMapper<SettlementItem> {
}
