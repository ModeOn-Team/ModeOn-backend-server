package com.modeon.backend.chat.service;

import com.modeon.backend.chat.dto.ChatMessageDto;
import com.modeon.backend.chat.dto.ChatRoomDto;
import com.modeon.backend.chat.entity.ChatMessage;
import com.modeon.backend.chat.entity.ChatRoom;
import com.modeon.backend.chat.repository.ChatMessageRepository;
import com.modeon.backend.chat.repository.ChatRoomRepository;
import com.modeon.backend.dto.UserDto;
import com.modeon.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;

    /**
     * 사용자의 채팅방 조회 또는 생성
     * 최초 접속 시 채팅방 자동 생성
     */
    @Transactional
    public ChatRoomDto getOrCreateChatRoom(Long userId) {
        ChatRoom chatRoom = chatRoomRepository.findFirstByUserIdAndIsActiveTrueOrderByCreatedAtDesc(userId)
                .orElseGet(() -> {
                    ChatRoom newRoom = ChatRoom.builder()
                            .userId(userId)
                            .isActive(true)
                            .build();
                    return chatRoomRepository.save(newRoom);
                });
        
        return ChatRoomDto.from(chatRoom);
    }

    /**
     * 메시지 저장 (DB 저장)
     */
    @Transactional
    public ChatMessageDto saveMessage(ChatMessageDto chatMessageDto) {
        ChatMessage.MessageType messageType = ChatMessage.MessageType.valueOf(
                chatMessageDto.getMessageType());
        
        ChatMessage chatMessage = ChatMessage.builder()
                .roomId(chatMessageDto.getRoomId())
                .userId(chatMessageDto.getUserId())
                .adminId(chatMessageDto.getAdminId())
                .content(chatMessageDto.getMessage())
                .messageType(messageType)
                .metadata(chatMessageDto.getMetadata())
                .build();
        
        ChatMessage savedMessage = chatMessageRepository.save(chatMessage);
        log.info("메시지 저장 완료: roomId={}, messageId={}", savedMessage.getRoomId(), savedMessage.getId());
        
        return ChatMessageDto.from(savedMessage);
    }

    /**
     * 채팅방의 메시지 목록 조회
     */
    public List<ChatMessageDto> getChatMessages(Long roomId) {
        List<ChatMessage> messages = chatMessageRepository.findByRoomIdOrderByCreatedAtAsc(roomId);
        return messages.stream()
                .map(ChatMessageDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 관리자가 관리할 수 있는 모든 채팅방 목록 조회
     */
    public List<ChatRoomDto> getAllActiveChatRooms() {
        List<ChatRoom> rooms = chatRoomRepository.findByIsActiveTrue();
        return rooms.stream()
                .map(room -> {
                    ChatRoomDto dto = ChatRoomDto.from(room);
                    // 사용자 정보 조회 및 매핑
                    if (room.getUserId() != null) {
                        userRepository.findById(room.getUserId())
                                .ifPresent(user -> dto.setOtherUser(UserDto.fromEntity(user)));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 특정 관리자가 담당하는 채팅방 목록 조회
     */
    public List<ChatRoomDto> getChatRoomsByAdminId(Long adminId) {
        List<ChatRoom> rooms = chatRoomRepository.findByAdminId(adminId);
        return rooms.stream()
                .map(room -> {
                    ChatRoomDto dto = ChatRoomDto.from(room);
                    // 사용자 정보 조회 및 매핑
                    if (room.getUserId() != null) {
                        userRepository.findById(room.getUserId())
                                .ifPresent(user -> dto.setOtherUser(UserDto.fromEntity(user)));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * 관리자 채팅방 할당
     */
    @Transactional
    public ChatRoomDto assignAdminToRoom(Long roomId, Long adminId) {
        ChatRoom chatRoom = chatRoomRepository.findByRoomIdAndIsActiveTrue(roomId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 채팅방입니다."));
        
        chatRoom.assignAdmin(adminId);
        ChatRoom savedRoom = chatRoomRepository.save(chatRoom);
        return ChatRoomDto.from(savedRoom);
    }

    /**
     * 채팅방 존재 여부 확인
     */
    public boolean existsRoom(Long roomId) {
        return chatRoomRepository.findByRoomIdAndIsActiveTrue(roomId).isPresent();
    }
}
