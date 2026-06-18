package com.atlas.common.message.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;

/**
 * Redis Pub/Sub 配置 — 跨实例消息广播 /
 * Redis Pub/Sub configuration — cross-instance message broadcast
 *
 * <p>当多个 atlas-message 实例部署时，一个实例收到的推送请求通过 Redis Pub/Sub 广播到其他实例，
 * 确保所有实例上的 WebSocket 连接都能收到消息。 /
 * When multiple atlas-message instances are deployed, a push request received by one instance
 * is broadcast to other instances via Redis Pub/Sub, ensuring WebSocket connections on all
 * instances receive the message.</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Configuration
public class RedisPubSubConfig {

    public static final String MESSAGE_CHANNEL = "atlas:message:push";

    @Bean
    public ChannelTopic messageTopic() {
        return new ChannelTopic(MESSAGE_CHANNEL);
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(RedisMessageSubscriber subscriber) {
        return new MessageListenerAdapter(subscriber, "onMessage");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter,
            ChannelTopic topic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(listenerAdapter, topic);
        return container;
    }
}
