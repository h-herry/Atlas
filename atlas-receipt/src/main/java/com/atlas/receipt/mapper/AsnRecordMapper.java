package com.atlas.receipt.mapper;

import com.atlas.receipt.entity.AsnRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * ASN 预先发货通知 Mapper / ASN record Mapper
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface AsnRecordMapper extends BaseMapper<AsnRecord> {
}
