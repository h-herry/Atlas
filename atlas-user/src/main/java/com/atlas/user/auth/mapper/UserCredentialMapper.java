package com.atlas.user.auth.mapper;

import com.atlas.user.auth.entity.UserCredential;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 用户凭据 Mapper /
 * User credential mapper
 */
@Mapper
public interface UserCredentialMapper extends BaseMapper<UserCredential> {

    /** 根据 userId 和 channel 查询凭据 / Query credential by userId and channel */
    @Select("SELECT * FROM user_credential WHERE user_id = #{userId} AND channel = #{channel}")
    UserCredential selectByUserIdAndChannel(@Param("userId") Long userId, @Param("channel") String channel);
}
