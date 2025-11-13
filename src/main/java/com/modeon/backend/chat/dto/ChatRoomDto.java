package com.modeon.backend.chat.dto;

import com.modeon.backend.chat.entity.ChatRoom;
import com.modeon.backend.dto.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDto {
    
    private Long roomId;
    private Long userId;
    private Long adminId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDto otherUser;  // 사용자 정보 (프론트엔드 필수)

    public static ChatRoomDto from(ChatRoom chatRoom) {
        return ChatRoomDto.builder()
                .roomId(chatRoom.getRoomId())
                .userId(chatRoom.getUserId())
                .adminId(chatRoom.getAdminId())
                .isActive(chatRoom.getIsActive())
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .build();
    }
}

