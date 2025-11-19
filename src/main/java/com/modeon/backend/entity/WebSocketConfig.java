package com.modeon.backend.entity;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.util.StringUtils;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.springframework.web.socket.messaging.SessionSubscribeEvent;
import org.springframework.web.socket.messaging.SessionUnsubscribeEvent;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import javax.crypto.SecretKey;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${JWT_SECRET}")
    private String secretKey;

    private SecretKey key;

    @PostConstruct
    public void init() {
        // ✅ JwtService와 동일하게 Base64 decode
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 메시지 브로커 설정
        // 클라이언트가 메시지를 보낼 때 사용하는 prefix
        registry.setApplicationDestinationPrefixes("/pub");
        
        // 구독(subscribe) 경로 설정
        // 클라이언트가 구독할 때 사용하는 prefix
        // heartbeat 설정: 클라이언트와 동일하게 4000ms (4초)
        // TaskScheduler가 필요하므로 taskScheduler() Bean 등록 필요
        registry.enableSimpleBroker("/sub")
                .setHeartbeatValue(new long[]{4000, 4000})
                .setTaskScheduler(taskScheduler());
    }

    /**
     * Heartbeat를 위한 TaskScheduler Bean
     */
    @Bean
    public TaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(1);
        scheduler.setThreadNamePrefix("websocket-heartbeat-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(60);
        scheduler.initialize();
        return scheduler;
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트 등록
        registry.addEndpoint("/ws/chat")
                .setAllowedOriginPatterns("*")
                .addInterceptors(handshakeInterceptor())
                .withSockJS();
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // JWT 인증을 위한 인터셉터 등록
        registration.interceptors(stompChannelInterceptor());
    }

    @Bean
    public HandshakeInterceptor handshakeInterceptor() {
        return new HttpSessionHandshakeInterceptor() {
            @Override
            public boolean beforeHandshake(
                    org.springframework.http.server.ServerHttpRequest request,
                    org.springframework.http.server.ServerHttpResponse response,
                    org.springframework.web.socket.WebSocketHandler wsHandler,
                    Map<String, Object> attributes) throws Exception {
                
                // 쿼리 파라미터에서 토큰 추출
                String token = null;
                if (request instanceof org.springframework.http.server.ServletServerHttpRequest) {
                    org.springframework.http.server.ServletServerHttpRequest servletRequest =
                            (org.springframework.http.server.ServletServerHttpRequest) request;
                    token = servletRequest.getServletRequest().getParameter("token");
                }
                
                // Authorization 헤더에서도 토큰 확인
                if (token == null) {
                    String authHeader = request.getHeaders().getFirst("Authorization");
                    if (authHeader != null && authHeader.startsWith("Bearer ")) {
                        token = authHeader.substring(7);
                    }
                }
                
                // 토큰이 있으면 attributes에 저장 (검증은 STOMP CONNECT에서 수행)
                if (token != null && StringUtils.hasText(token)) {
                    attributes.put("token", token);
                    log.info("WebSocket handshake: 토큰을 attributes에 저장했습니다.");
                } else {
                    log.warn("WebSocket handshake: 토큰이 없습니다. STOMP CONNECT에서 검증합니다.");
                }
                
                // handshake는 항상 허용 (인증은 STOMP CONNECT에서 수행)
                return true;
            }
        };
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
                                Claims claims = Jwts.parserBuilder()
                                        .setSigningKey(key)
                                        .build()
                                        .parseClaimsJws(token)
                                        .getBody();

                                // 사용자 정보를 세션에 저장
                                String userId = claims.getSubject();

                                accessor.setUser(new StompPrincipal(userId));

                                log.info("WebSocket 연결 인증 성공: userId={}", userId);
                            } catch (io.jsonwebtoken.security.SignatureException e) {
                                log.error("WebSocket 연결 인증 실패: JWT 서명이 일치하지 않습니다. JWT_SECRET을 확인하세요.");
                                throw new MessageDeliveryException(message, "인증에 실패했습니다: JWT 서명이 일치하지 않습니다.");
                            } catch (io.jsonwebtoken.ExpiredJwtException e) {
                                log.error("WebSocket 연결 인증 실패: JWT 토큰이 만료되었습니다.");
                                throw new MessageDeliveryException(message, "인증에 실패했습니다: 토큰이 만료되었습니다.");
                            } catch (Exception e) {
                                log.error("WebSocket 연결 인증 실패: {}", e.getMessage(), e);
                                throw new MessageDeliveryException(message, "인증에 실패했습니다: " + e.getMessage());
                            }
                        } else {
                            log.warn("유효하지 않은 토큰 형식입니다. 연결을 허용하지만 메시지 전송 시 인증이 필요합니다.");
                            // 토큰 형식이 잘못되었어도 연결은 허용 (메시지 전송 시 검증)
                        }
                    } else {
                        log.warn("인증 토큰이 없습니다. 연결을 허용하지만 메시지 전송 시 인증이 필요합니다.");
                        // 토큰이 없어도 연결은 허용 (메시지 전송 시 검증)
                    }
                }
                
                return message;
            }
        };
    }

    // WebSocket 세션 이벤트 리스너
    @EventListener
    public void handleSessionConnectEvent(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userId = accessor.getUser() != null ? accessor.getUser().getName() : "unknown";
        log.info("WebSocket 세션 연결: sessionId={}, userId={}, command={}", 
                sessionId, userId, accessor.getCommand());
    }

    @EventListener
    public void handleSessionDisconnectEvent(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userId = accessor.getUser() != null ? accessor.getUser().getName() : "unknown";
        
        // CloseStatus 정보 추출
        org.springframework.web.socket.CloseStatus closeStatus = event.getCloseStatus();
        String closeStatusInfo = closeStatus != null 
                ? String.format("code=%d, reason=%s", closeStatus.getCode(), closeStatus.getReason())
                : "unknown";
        
        log.warn("WebSocket 세션 종료: sessionId={}, userId={}, closeStatus={}, command={}", 
                sessionId, userId, closeStatusInfo, accessor.getCommand());
        
        // 세션 종료 원인 상세 로깅
        if (accessor.getSessionAttributes() != null && !accessor.getSessionAttributes().isEmpty()) {
            log.warn("세션 속성: {}", accessor.getSessionAttributes());
        }
        if (accessor.toNativeHeaderMap() != null && !accessor.toNativeHeaderMap().isEmpty()) {
            log.warn("네이티브 헤더: {}", accessor.toNativeHeaderMap());
        }
    }

    @EventListener
    public void handleSessionSubscribeEvent(SessionSubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String destination = accessor.getDestination();
        String userId = accessor.getUser() != null ? accessor.getUser().getName() : "unknown";
        
        // 채팅방 구독 시 destination에서 roomId 추출하여 권한 확인
        if (destination != null && destination.startsWith("/sub/chatroom/")) {
            try {
                String roomIdStr = destination.replace("/sub/chatroom/", "");
                Long roomId = Long.parseLong(roomIdStr);
                // 여기서는 로깅만 하고, 실제 권한 검증은 메시지 전송 시 수행
                log.info("WebSocket 구독: sessionId={}, userId={}, destination={}, roomId={}", 
                        sessionId, userId, destination, roomId);
            } catch (NumberFormatException e) {
                log.warn("유효하지 않은 채팅방 ID: {}", destination);
            }
        } else {
            log.info("WebSocket 구독: sessionId={}, userId={}, destination={}", 
                    sessionId, userId, destination);
        }
    }

    @EventListener
    public void handleSessionUnsubscribeEvent(SessionUnsubscribeEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();
        String userId = accessor.getUser() != null ? accessor.getUser().getName() : "unknown";
        log.info("WebSocket 구독 해제: sessionId={}, userId={}", 
                sessionId, userId);
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
