package com.atlas.quality.mapper;

import com.atlas.quality.entity.NcrRecord;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 不合格品处理 NCR Mapper / Non-Conformance Report Mapper
 *
 * @author Atlas Team
 * @since 2.1.0
 */
@Mapper
public interface NcrRecordMapper extends BaseMapper<NcrRecord> {
}
