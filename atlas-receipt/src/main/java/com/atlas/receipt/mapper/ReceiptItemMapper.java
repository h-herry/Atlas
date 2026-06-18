package com.atlas.receipt.mapper;

import com.atlas.receipt.entity.ReceiptItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 收货明细表 Mapper / Receipt item Mapper
 *
 * @author Atlas Team
 * @since 1.0.0
 */
@Mapper
public interface ReceiptItemMapper extends BaseMapper<ReceiptItem> {
}
