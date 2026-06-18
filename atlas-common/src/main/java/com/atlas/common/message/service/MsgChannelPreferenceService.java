package com.atlas.common.message.service;

import com.atlas.common.message.mapper.MsgChannelPreferenceMapper;
import com.atlas.common.message.model.MsgChannelPreference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 消息渠道偏好 Service — 用户级 [消息类型 → 渠道] 偏好配置 /
 * Message channel preference Service — user-level [message type → channel] preference configuration
 *
 * <p>每种消息类型独立配置 WebSocket / 邮件 / 短信开关，支持静默时段 /
 * Each message type independently configurable for WebSocket / Mail / SMS with quiet hours</p>
 *
 * @since 1.2.22
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MsgChannelPreferenceService {

    private final MsgChannelPreferenceMapper preferenceMapper;

    /**
     * 创建或更新渠道偏好 / Create or update channel preference
     */
    @Transactional(rollbackFor = Exception.class)
    public MsgChannelPreference save(MsgChannelPreference preference) {
        // 查询是否已存在同用户同类型的配置 / Check for existing config for same user + event type
        LambdaQueryWrapper<MsgChannelPreference> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MsgChannelPreference::getUserId, preference.getUserId())
               .eq(MsgChannelPreference::getEventType, preference.getEventType());
        MsgChannelPreference existing = preferenceMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setWsEnabled(preference.getWsEnabled());
            existing.setMailEnabled(preference.getMailEnabled());
            existing.setSmsEnabled(preference.getSmsEnabled());
            existing.setQuietStart(preference.getQuietStart());
            existing.setQuietEnd(preference.getQuietEnd());
            preferenceMapper.updateById(existing);
            log.info("渠道偏好已更新: userId={}, eventType={}", preference.getUserId(), preference.getEventType());
            return existing;
        } else {
            preferenceMapper.insert(preference);
            log.info("渠道偏好已创建: userId={}, eventType={}", preference.getUserId(), preference.getEventType());
            return preference;
        }
    }

    /**
     * 查询用户所有渠道偏好 / Query all preferences for a user
     */
    public List<MsgChannelPreference> listByUser(Long userId) {
        return preferenceMapper.findByUserId(userId);
    }

    /**
     * 按事件类型查询用户偏好 / Query user preference by event type
     */
    public MsgChannelPreference getByUserAndEvent(Long userId, String eventType) {
        return preferenceMapper.findByUserAndEvent(userId, eventType);
    }

    /**
     * 删除偏好 / Delete preference
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long prefId) {
        preferenceMapper.deleteById(prefId);
        log.info("渠道偏好已删除: prefId={}", prefId);
    }

    /**
     * 批量初始化用户默认偏好（所有类型默认仅 WS 启用）/ Batch initialize default preferences (WS only)
     */
    @Transactional(rollbackFor = Exception.class)
    public void initDefaults(Long userId) {
        String[] eventTypes = {"ORDER", "DELIVERY", "SETTLEMENT", "SYSTEM", "APPROVAL", "QUALITY"};
        for (String eventType : eventTypes) {
            MsgChannelPreference pref = new MsgChannelPreference();
            pref.setUserId(userId);
            pref.setEventType(eventType);
            pref.setWsEnabled(1);
            pref.setMailEnabled(0);
            pref.setSmsEnabled(0);
            preferenceMapper.insert(pref);
        }
        log.info("用户 {} 渠道偏好已初始化", userId);
    }
}
