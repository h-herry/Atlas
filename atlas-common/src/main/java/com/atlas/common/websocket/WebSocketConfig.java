package com.atlas.common.websocket;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket 配置类 — STOMP over SockJS /
 * WebSocket configuration class — STOMP over SockJS
 *
 * <p>端点: /ws/message（SockJS 兼容） /
 * Endpoint: /ws/message (SockJS compatible)</p>
 *
 * <p>消息代理: /topic 广播, /queue 点对点 /
 * Message broker: /topic for broadcast, /queue for point-to-point</p>
 *
 * @author Atlas Team
 * @since 1.2.10
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketAuthInterceptor webSocketAuthInterceptor;

    /**
     * 配置消息代理 / Configure message broker
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理，订阅前缀 / Enable simple message broker with subscription prefix
        registry.enableSimpleBroker("/topic", "/queue");
        // 应用程序消息前缀（客户端发送到服务器的目标前缀） / Application message prefix (client → server)
        registry.setApplicationDestinationPrefixes("/app");
        // 用户级别消息前缀（convertAndSendToUser 使用） / User-level message prefix (for convertAndSendToUser)
        registry.setUserDestinationPrefix("/user");
        // 心跳间隔 10 秒 / Heartbeat interval 10 seconds
        registry.setCacheLimit(8192);
    }

    /**
     * 注册 STOMP 端点 / Register STOMP endpoints
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws/message")
                .setAllowedOriginPatterns("*")
                .withSockJS()
                .setHeartbeatTime(10_000)
                .setDisconnectDelay(30_000);
    }

    /**
     * 配置客户端入站通道拦截器（认证） / Configure client inbound channel interceptor (auth)
     */
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketAuthInterceptor);
    }
}
