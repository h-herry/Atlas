package com.atlas.message;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Atlas 消息推送微服务启动类 /
 * Atlas message push microservice bootstrap class
 *
 * <p>核心能力: /
 * Core capabilities:
 * <ul>
 *   <li>WebSocket 实时消息推送（STOMP over SockJS）/ Real-time message push via WebSocket (STOMP over SockJS)</li>
 *   <li>消息记录持久化与历史查询 / Message record persistence and history query</li>
 *   <li>跨实例广播（Redis Pub/Sub）/ Cross-instance broadcast (Redis Pub/Sub)</li>
 *   <li>业务事件监听与自动推送编排 / Business event listening and auto-push orchestration</li>
 * </ul>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@SpringBootApplication(scanBasePackages = {"com.atlas.message", "com.atlas.common"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.atlas.message.feign")
@EnableScheduling
@EnableAsync
@MapperScan("com.atlas.message.mapper")
public class MessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(MessageApplication.class, args);
    }
}
