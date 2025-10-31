package com.modeon.backend.chat.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeon.backend.chat.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@Slf4j
@RequiredArgsConstructor
@Service
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;
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
