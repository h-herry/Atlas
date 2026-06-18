package com.atlas.common.mapper;

import com.atlas.common.entity.AuditLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 审计日志 Mapper / Audit log Mapper
 *
 * @author Atlas Team
 * @since 2.0.0
 */
@Mapper
public interface AuditLogMapper extends BaseMapper<AuditLog> {
}
