package com.atlas.common.mq.producer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

/**
 * 消息生产者抽象基类 — 封装发送逻辑和重试 /
 * Message producer abstract base — encapsulates send logic and retry
 * <p>
 * 子类只需要实现 topic() + tags() 即可发送消息 /
 * Subclasses only need to implement topic() + tags() to send messages
 */
@Slf4j
public abstract class AbstractMessageProducer {

    @Autowired
    protected RocketMQTemplate rocketMQTemplate;

    /** 主题，由子类指定 / Topic, specified by subclass */
    protected abstract String topic();

    /** 标签，由子类指定 / Tags, specified by subclass */
    protected abstract String tags();

    /**
     * 同步发送普通消息 / Synchronously send normal message
     */
    protected SendResult sendSync(String keys, Object payload) {
        String destination = topic() + ":" + tags();
        Message<Object> message = MessageBuilder.withPayload(payload)
                .setHeader("KEYS", keys)
                .build();
        SendResult result = rocketMQTemplate.syncSend(destination, message);
        if (result.getSendStatus() != SendStatus.SEND_OK) {
            log.error("MQ发送失败: topic={} keys={} status={}", topic(), keys, result.getSendStatus());
        }
        return result;
    }

    /**
     * 异步发送（推荐用于普通业务通知） / Async send (recommended for general business notifications)
     */
    public void sendAsync(String keys, Object payload) {
        String destination = topic() + ":" + tags();
        Message<Object> message = MessageBuilder.withPayload(payload)
                .setHeader("KEYS", keys)
                .build();
        rocketMQTemplate.asyncSend(destination, message, new org.apache.rocketmq.client.producer.SendCallback() {
            @Override
            public void onSuccess(SendResult result) {
                log.debug("MQ异步发送成功: topic={} keys={} msgId={}", topic(), keys, result.getMsgId());
            }

            @Override
            public void onException(Throwable e) {
                log.error("MQ异步发送失败: topic={} keys={}", topic(), keys, e);
            }
        });
    }
}
