package com.modeon.backend.chat.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeon.backend.chat.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisPublisher {

    private final StringRedisTemplate stringRedisTemplate;
    private final ChannelTopic channelTopic;
    private final ObjectMapper objectMapper;

    public void publish(ChatMessageDto chatMessage) {
        try {
            String message = objectMapper.writeValueAsString(chatMessage);
            stringRedisTemplate.convertAndSend(channelTopic.getTopic(), message);
            log.info("Redis에 메시지 발행: roomId={}, sender={}", 
                    chatMessage.getRoomId(), chatMessage.getSender());
        } catch (Exception e) {
            log.error("Redis 메시지 발행 중 오류 발생", e);
        }
    }
}
