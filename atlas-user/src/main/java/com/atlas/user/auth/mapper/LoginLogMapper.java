package com.atlas.user.auth.mapper;

import com.atlas.user.auth.entity.LoginLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 登录日志 Mapper /
 * Login log mapper
 */
@Mapper
public interface LoginLogMapper extends BaseMapper<LoginLog> {
}
