package com.modeon.backend.chat.dto;

import com.modeon.backend.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    
    private Long id;  // 메시지 고유 ID (프론트엔드 key prop용)
    private Long roomId;
    private String sender;  // "USER" or "ADMIN"
    private String message;  // content
    private String messageType;  // TEXT, IMAGE, FILE, SYSTEM, EMOJI, REPLY
    private String metadata;  // 메타데이터 (JSON String 형태)
    private LocalDateTime createdAt;
    private Long userId;
    private Long adminId;

    public static ChatMessageDto from(ChatMessage chatMessage) {
        return ChatMessageDto.builder()
                .id(chatMessage.getId())  // 메시지 ID 추가
                .roomId(chatMessage.getRoomId())
                .sender(chatMessage.getSender())
                .message(chatMessage.getContent())
                .messageType(chatMessage.getMessageType().name())
                .metadata(chatMessage.getMetadata())
                .createdAt(chatMessage.getCreatedAt())
                .userId(chatMessage.getUserId())
                .adminId(chatMessage.getAdminId())
                .build();
    }
}

