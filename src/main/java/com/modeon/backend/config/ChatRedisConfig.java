package com.modeon.backend.chat.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.modeon.backend.chat.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.messaging.simp.SimpMessageSendingOperations;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ChatRedisConfig {

    private final SimpMessageSendingOperations messagingTemplate;

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory factory) {
        return new StringRedisTemplate(factory);
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }

    @Bean
    public ChannelTopic channelTopic() {
        return new ChannelTopic("chatroom");
    }

    @Bean
    public MessageListenerAdapter messageListenerAdapter(StringRedisTemplate stringRedisTemplate, ObjectMapper objectMapper) {
        return new MessageListenerAdapter(new RedisSubscriber(stringRedisTemplate, objectMapper, messagingTemplate), "onMessage");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(
            RedisConnectionFactory connectionFactory,
            MessageListenerAdapter messageListenerAdapter,
            ChannelTopic channelTopic) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(messageListenerAdapter, channelTopic);
        return container;
    }

    /**
     * Redis 메시지 구독자 (내부 클래스)
     */
    @RequiredArgsConstructor
    private static class RedisSubscriber implements MessageListener {
        private final StringRedisTemplate stringRedisTemplate;
        private final ObjectMapper objectMapper;
        private final SimpMessageSendingOperations messagingTemplate;

        @Override
        public void onMessage(Message message, byte[] pattern) {
            try {
                // Redis에서 받은 메시지 역직렬화
                String publishMessage = stringRedisTemplate.getStringSerializer().deserialize(message.getBody());
                ChatMessageDto chatMessage = objectMapper.readValue(publishMessage, ChatMessageDto.class);
                
                // 해당 채팅방 구독자들에게 메시지 전송
                messagingTemplate.convertAndSend("/sub/chatroom/" + chatMessage.getRoomId(), chatMessage);
                
                log.info("Redis에서 메시지 수신 및 브로드캐스트: roomId={}, sender={}", 
                        chatMessage.getRoomId(), chatMessage.getSender());
            } catch (Exception e) {
                log.error("Redis 메시지 처리 중 오류 발생", e);
            }
        }
    }
}

