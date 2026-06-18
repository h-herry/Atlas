package com.atlas.open.mapper;

import com.atlas.open.entity.ApiIntegrationLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * API 集成调用日志 Mapper / API integration call log Mapper
 */
@Mapper
public interface ApiIntegrationLogMapper extends BaseMapper<ApiIntegrationLog> {
}
