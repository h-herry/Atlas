package com.atlas.open.mapper;

import com.atlas.open.entity.WebhookSubscription;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * Webhook 订阅 Mapper / Webhook subscription Mapper
 */
@Mapper
public interface WebhookSubscriptionMapper extends BaseMapper<WebhookSubscription> {
}
