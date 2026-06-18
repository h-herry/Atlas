package com.atlas.open.mapper;

import com.atlas.open.entity.ApiCallLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * API 调用日志 Mapper / API call log Mapper
 */
@Mapper
public interface ApiCallLogMapper extends BaseMapper<ApiCallLog> {
}
