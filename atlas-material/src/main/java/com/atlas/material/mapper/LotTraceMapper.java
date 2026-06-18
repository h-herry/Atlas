package com.atlas.material.mapper;

import com.atlas.material.entity.LotTrace;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 来料批次追溯 Mapper / Incoming lot trace Mapper
 *
 * @author Atlas Team
 * @since 1.3.0
 */
@Mapper
public interface LotTraceMapper extends BaseMapper<LotTrace> {
}
