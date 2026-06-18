package com.atlas.purchase.inquiry.mapper;

import com.atlas.purchase.inquiry.entity.Quote;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 报价记录 Mapper / Quote Mapper
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface QuoteMapper extends BaseMapper<Quote> {
}
