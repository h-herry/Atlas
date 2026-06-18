package com.atlas.message.controller;

import com.atlas.common.security.annotation.RequirePermission;
import com.atlas.message.model.MsgChannelPreference;
import com.atlas.message.service.MsgChannelPreferenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 消息渠道偏好 Controller — 用户级渠道偏好配置 /
 * Message channel preference Controller — user-level channel preference configuration
 *
 * @since 1.2.22
 */
@RestController
@RequestMapping("/api/message/preference")
@RequiredArgsConstructor
@Tag(name = "消息渠道偏好 / Message Channel Preference")
public class MsgChannelPreferenceController {

    private final MsgChannelPreferenceService preferenceService;

    /**
     * 创建或更新渠道偏好 / Create or update channel preference
     */
    @PostMapping
    public MsgChannelPreference save(@RequestBody MsgChannelPreference preference) {
        return preferenceService.save(preference);
    }

    /**
     * 查询用户所有渠道偏好 / Query all preferences for a user
     */
    @GetMapping("/user/{userId}")
    @RequirePermission("message:manage")
    public List<MsgChannelPreference> listByUser(@PathVariable Long userId) {
        return preferenceService.listByUser(userId);
    }

    /**
     * 按事件类型查询偏好 / Query preference by event type
     */
    @GetMapping("/user/{userId}/event/{eventType}")
    public MsgChannelPreference getByEvent(@PathVariable Long userId,
                                            @PathVariable String eventType) {
        return preferenceService.getByUserAndEvent(userId, eventType);
    }

    /**
     * 初始化用户默认偏好（所有类型默认仅 WS 启用）/ Initialize default preferences
     */
    @PostMapping("/user/{userId}/init")
    public void initDefaults(@PathVariable Long userId) {
        preferenceService.initDefaults(userId);
    }

    /**
     * 删除偏好 / Delete preference
     */
    @DeleteMapping("/{prefId}")
    @RequirePermission("message:manage")
    public void delete(@PathVariable Long prefId) {
        preferenceService.delete(prefId);
    }
}
