package com.atlas.receipt.mapper;

import com.atlas.receipt.entity.Receipt;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货单主表 Mapper / Receipt master Mapper
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Mapper
public interface ReceiptMapper extends BaseMapper<Receipt> {
}
