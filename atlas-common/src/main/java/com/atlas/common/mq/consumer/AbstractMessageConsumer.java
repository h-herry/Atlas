package com.atlas.common.mq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;

/**
 * 消息消费者抽象基类 — 子类只需指定 topic/tag 并实现 handleMessage /
 * Message consumer abstract base — subclasses only need to specify topic/tag and implement handleMessage
 * <p>
 * 使用方式 / Usage：子类继承 + @Service + @RocketMQMessageListener
 * <pre>
 * &#064;Service
 * &#064;RocketMQMessageListener(topic = "order", consumerGroup = "order-consumer", selectorExpression = "created")
 * public class OrderCreatedConsumer extends AbstractMessageConsumer&lt;OrderEvent&gt; {
 *     &#064;Override
 *     protected void handleMessage(OrderEvent event) { ... }
 * }
 * </pre>
 */
@Slf4j
public abstract class AbstractMessageConsumer<T> {

    /**
     * 消费单条消息 — 内部已做异常捕获和日志 /
     * Consume single message — internal exception catching and logging done
     */
    public void onMessage(T message) {
        try {
            log.debug("MQ消费: topic=... payload={}", message);
            handleMessage(message);
        } catch (Exception e) {
            log.error("MQ消费异常: payload={}", message, e);
            // 阶段三实现：消费失败写入死信表，人工补偿 / Phase 3: write to dead letter table on failure, manual compensation
        }
    }

    /**
     * 业务处理逻辑，子类实现 / Business processing logic, implemented by subclass
     */
    protected abstract void handleMessage(T message);
}
