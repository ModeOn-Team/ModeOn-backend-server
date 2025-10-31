package com.modeon.backend.chat.config;

import com.modeon.backend.chat.interceptor.StompChannelInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompChannelInterceptor stompChannelInterceptor;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커 설정
        // 클라이언트가 메시지를 보낼 때 사용하는 prefix
        registry.setApplicationDestinationPrefixes("/pub");
        
        // 구독(subscribe) 경로 설정
        // 클라이언트가 구독할 때 사용하는 prefix
        registry.enableSimpleBroker("/sub");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // JWT 인증을 위한 인터셉터 등록
        registration.interceptors(stompChannelInterceptor);
    }
}
