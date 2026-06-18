package com.atlas.receipt.mapper;

import com.atlas.receipt.entity.ReceiptOutbox;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货确认本地消息表 Mapper / Receipt outbox (local message table) Mapper
 */
@Mapper
public interface ReceiptOutboxMapper extends BaseMapper<ReceiptOutbox> {
}
