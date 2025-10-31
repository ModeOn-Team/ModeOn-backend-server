package com.modeon.backend.chat.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${jwt.secret}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

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
        registration.interceptors(stompChannelInterceptor());
    }

    @Bean
    public ChannelInterceptor stompChannelInterceptor() {
        return new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // CONNECT 시 JWT 토큰 검증
                    List<String> tokenList = accessor.getNativeHeader("Authorization");
                    
                    if (tokenList != null && !tokenList.isEmpty()) {
                        String token = tokenList.get(0);
                        
                        if (StringUtils.hasText(token) && token.startsWith("Bearer ")) {
                            token = token.substring(7);
                            
                            try {
                                // JWT 토큰 검증
                                Claims claims = Jwts.parser()
                                        .verifyWith(key)
                                        .build()
                                        .parseSignedClaims(token)
                                        .getPayload();
                                
                                // 사용자 정보를 세션에 저장
                                String userId = claims.getSubject();
                                accessor.setUser(new StompPrincipal(userId));
                                
                                log.info("WebSocket 연결 인증 성공: userId={}", userId);
                            } catch (Exception e) {
                                log.error("WebSocket 연결 인증 실패: {}", e.getMessage());
                                throw new RuntimeException("인증에 실패했습니다.", e);
                            }
                        } else {
                            throw new RuntimeException("유효하지 않은 토큰 형식입니다.");
                        }
                    } else {
                        throw new RuntimeException("인증 토큰이 없습니다.");
                    }
                }
                
                return message;
            }
        };
    }

    // Stomp 사용자를 나타내는 간단한 클래스
    public static class StompPrincipal implements java.security.Principal {
        private final String name;

        public StompPrincipal(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
