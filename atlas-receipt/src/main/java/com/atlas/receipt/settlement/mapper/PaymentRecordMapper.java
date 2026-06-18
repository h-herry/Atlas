package com.atlas.receipt.settlement.mapper;

import com.atlas.receipt.settlement.entity.PaymentRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 付款记录 Mapper / Payment record Mapper
 *
 * @author Atlas Team
 * @since 1.2.401
 */
@Mapper
public interface PaymentRecordMapper extends BaseMapper<PaymentRecord> {
}
