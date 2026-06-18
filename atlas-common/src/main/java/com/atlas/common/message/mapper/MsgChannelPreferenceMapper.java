package com.atlas.common.message.mapper;

import com.atlas.common.message.model.MsgChannelPreference;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 消息渠道偏好 Mapper / Message channel preference Mapper
 *
 * @since 1.2.22
 */
@Mapper
public interface MsgChannelPreferenceMapper extends BaseMapper<MsgChannelPreference> {

    /**
     * 按事件类型查询用户偏好 / Query user preference by event type
     */
    @Select("SELECT * FROM msg_channel_preference WHERE user_id = #{userId} AND event_type = #{eventType}")
    MsgChannelPreference findByUserAndEvent(@Param("userId") Long userId, @Param("eventType") String eventType);

    /**
     * 查询用户所有渠道偏好 / Query all preferences for a user
     */
    @Select("SELECT * FROM msg_channel_preference WHERE user_id = #{userId}")
    java.util.List<MsgChannelPreference> findByUserId(@Param("userId") Long userId);
}
