package com.modeon.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.modeon.backend.chat.dto.ChatMessageDto;
import com.modeon.backend.chat.dto.ChatRoomDto;
import com.modeon.backend.chat.service.ChatService;
import com.modeon.backend.entity.User;
import com.modeon.backend.exception.AccessDeniedException;
import com.modeon.backend.exception.BadRequestException;
import com.modeon.backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chating")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ChannelTopic channelTopic;
    private final ObjectMapper objectMapper;
    private final AuthenticationService authenticationService;

    /**
     * 채팅방 접속 및 생성 (REST API)
     * POST /api/chating/join
     * 인증된 사용자만 자신의 채팅방에 접근 가능
     */
    @PostMapping("/join")
    public ResponseEntity<ChatRoomDto> joinChatRoom(
            @RequestParam Long userId,
            @AuthenticationPrincipal User currentUser) {
        // 인증된 사용자와 요청한 userId가 일치하는지 확인
        if (currentUser == null || currentUser.getId() != userId) {
            throw new AccessDeniedException("본인의 채팅방에만 접근할 수 있습니다.");
        }
        ChatRoomDto room = chatService.getOrCreateChatRoom(userId);
        return ResponseEntity.ok(room);
    }

    /**
     * WebSocket을 통한 메시지 전송
     * STOMP destination: /pub/chat.sendMessage
     * WebSocket 연결 시 인증된 사용자만 메시지 전송 가능
     */
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload ChatMessageDto chatMessageDto, 
                          SimpMessageHeaderAccessor accessor,
                          Principal principal) {
        try {
            // Principal에서 사용자 ID 추출
            if (principal == null || principal.getName() == null) {
                log.error("WebSocket 메시지 전송 실패: 인증되지 않은 사용자");
                throw new AccessDeniedException("인증되지 않은 사용자는 메시지를 전송할 수 없습니다.");
            }
            
            Long currentUserId = Long.parseLong(principal.getName());
            User currentUser = authenticationService.getCurrentUser();
            
            // 채팅방 접근 권한 확인
            validateChatRoomAccess(chatMessageDto.getRoomId(), currentUser);
            
            // 메시지 발신자 검증
            validateMessageSender(chatMessageDto, currentUser);
            
            log.info("메시지 수신: roomId={}, sender={}, message={}, userId={}", 
                    chatMessageDto.getRoomId(), 
                    chatMessageDto.getSender(), 
                    chatMessageDto.getMessage(),
                    currentUserId);
            
            // 1. DB에 메시지 저장
            ChatMessageDto savedMessage = chatService.saveMessage(chatMessageDto);
            
            // 2. Redis를 통해 메시지 발행 (실시간 브로드캐스트)
            publishToRedis(savedMessage);
            
        } catch (Exception e) {
            log.error("메시지 전송 중 오류 발생", e);
            throw e; // 예외를 다시 던져서 클라이언트에 전달
        }
    }

    /**
     * 텍스트 메시지 전송 (REST API)
     * POST /api/chating/message/text
     * 인증된 사용자만 메시지 전송 가능
     */
    @PostMapping("/message/text")
    public ResponseEntity<ChatMessageDto> sendTextMessage(
            @RequestBody ChatMessageDto chatMessageDto,
            @AuthenticationPrincipal User currentUser) {
        // 채팅방 접근 권한 확인
        validateChatRoomAccess(chatMessageDto.getRoomId(), currentUser);
        
        // 메시지 발신자 검증
        validateMessageSender(chatMessageDto, currentUser);
        // 메시지 타입 설정
        chatMessageDto = ChatMessageDto.builder()
                .roomId(chatMessageDto.getRoomId())
                .sender(chatMessageDto.getSender())
                .message(chatMessageDto.getMessage())
                .messageType("TEXT")
                .metadata(chatMessageDto.getMetadata())
                .userId(chatMessageDto.getUserId())
                .adminId(chatMessageDto.getAdminId())
                .build();
        
        // DB 저장
        ChatMessageDto savedMessage = chatService.saveMessage(chatMessageDto);
        
        // Redis 발행
        publishToRedis(savedMessage);
        
        return ResponseEntity.ok(savedMessage);
    }

    /**
     * 이미지 메시지 전송 (REST API)
     * POST /api/chating/message/image
     * metadata에 이미지 관련 정보 (파일명, 크기 등) JSON 형태로 전달 가능
     */
    @PostMapping("/message/image")
    public ResponseEntity<ChatMessageDto> sendImageMessage(
            @RequestBody ChatMessageDto chatMessageDto,
            @AuthenticationPrincipal User currentUser) {
        // 채팅방 접근 권한 확인
        validateChatRoomAccess(chatMessageDto.getRoomId(), currentUser);
        
        // 메시지 발신자 검증
        validateMessageSender(chatMessageDto, currentUser);
        chatMessageDto = ChatMessageDto.builder()
                .roomId(chatMessageDto.getRoomId())
                .sender(chatMessageDto.getSender())
                .message(chatMessageDto.getMessage())  // 이미지 URL
                .messageType("IMAGE")
                .metadata(chatMessageDto.getMetadata())  // 이미지 메타데이터 (파일명, 크기 등)
                .userId(chatMessageDto.getUserId())
                .adminId(chatMessageDto.getAdminId())
                .build();
        
        ChatMessageDto savedMessage = chatService.saveMessage(chatMessageDto);
        publishToRedis(savedMessage);
        
        return ResponseEntity.ok(savedMessage);
    }

    /**
     * 파일 메시지 전송 (REST API)
     * POST /api/chating/message/file
     * metadata에 파일 관련 정보 (파일명, 크기, 확장자 등) JSON 형태로 전달 가능
     */
    @PostMapping("/message/file")
    public ResponseEntity<ChatMessageDto> sendFileMessage(
            @RequestBody ChatMessageDto chatMessageDto,
            @AuthenticationPrincipal User currentUser) {
        // 채팅방 접근 권한 확인
        validateChatRoomAccess(chatMessageDto.getRoomId(), currentUser);
        
        // 메시지 발신자 검증
        validateMessageSender(chatMessageDto, currentUser);
        chatMessageDto = ChatMessageDto.builder()
                .roomId(chatMessageDto.getRoomId())
                .sender(chatMessageDto.getSender())
                .message(chatMessageDto.getMessage())  // 파일 URL
                .messageType("FILE")
                .metadata(chatMessageDto.getMetadata())  // 파일 메타데이터 (파일명, 크기, 확장자 등)
                .userId(chatMessageDto.getUserId())
                .adminId(chatMessageDto.getAdminId())
                .build();
        
        ChatMessageDto savedMessage = chatService.saveMessage(chatMessageDto);
        publishToRedis(savedMessage);
        
        return ResponseEntity.ok(savedMessage);
    }

    /**
     * 관리자 채팅 목록 조회
     * GET /api/chating/admin 또는 GET /api/chating/admin/
     */
    @GetMapping({"/admin", "/admin/"})
    public ResponseEntity<List<ChatRoomDto>> getAdminChatRooms(@RequestParam(required = false) Long adminId) {
        List<ChatRoomDto> rooms;
        if (adminId != null) {
            rooms = chatService.getChatRoomsByAdminId(adminId);
        } else {
            rooms = chatService.getAllActiveChatRooms();
        }
        return ResponseEntity.ok(rooms);
    }

    /**
     * 채팅방 정보 조회
     * GET /api/chating/room?roomId={roomId}
     */
    @GetMapping("/room")
    public ResponseEntity<ChatRoomDto> getChatRoom(
            @RequestParam Long roomId,
            @AuthenticationPrincipal User currentUser) {
        ChatRoomDto room = chatService.getChatRoom(roomId);
        
        // 채팅방 접근 권한 확인: 본인 또는 관리자만 접근 가능
        boolean isOwner = room.getUserId() != null && room.getUserId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN");
        boolean isAssignedAdmin = room.getAdminId() != null && room.getAdminId().equals(currentUser.getId());
        
        if (!isOwner && !isAdmin && !isAssignedAdmin) {
            throw new AccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
        }
        
        return ResponseEntity.ok(room);
    }

    /**
     * 채팅방의 메시지 목록 조회
     * GET /api/chating/messages?roomId={roomId}
     * 인증된 사용자만 자신의 채팅방 메시지를 조회 가능
     */
    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageDto>> getChatMessages(
            @RequestParam Long roomId,
            @AuthenticationPrincipal User currentUser) {
        // 채팅방 접근 권한 확인
        ChatRoomDto room = chatService.getChatRoom(roomId);
        
        boolean isOwner = room.getUserId() != null && room.getUserId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN");
        boolean isAssignedAdmin = room.getAdminId() != null && room.getAdminId().equals(currentUser.getId());
        
        if (!isOwner && !isAdmin && !isAssignedAdmin) {
            throw new AccessDeniedException("해당 채팅방의 메시지를 조회할 권한이 없습니다.");
        }
        
        List<ChatMessageDto> messages = chatService.getChatMessages(roomId);
        return ResponseEntity.ok(messages);
    }

    /**
     * 채팅방 접근 권한 검증
     */
    private void validateChatRoomAccess(Long roomId, User currentUser) {
        ChatRoomDto room = chatService.getChatRoom(roomId);
        
        boolean isOwner = room.getUserId() != null && room.getUserId().equals(currentUser.getId());
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN");
        boolean isAssignedAdmin = room.getAdminId() != null && room.getAdminId().equals(currentUser.getId());
        
        if (!isOwner && !isAdmin && !isAssignedAdmin) {
            throw new AccessDeniedException("해당 채팅방에 접근할 권한이 없습니다.");
        }
    }

    /**
     * 메시지 발신자 검증
     */
    private void validateMessageSender(ChatMessageDto chatMessageDto, User currentUser) {
        boolean isAdmin = currentUser.getRole() != null && currentUser.getRole().equalsIgnoreCase("ROLE_ADMIN");
        
        if ("USER".equals(chatMessageDto.getSender())) {
            // USER로 발신하는 경우, userId 필수
            if (chatMessageDto.getUserId() == null) {
                throw new BadRequestException("일반 사용자는 userId가 필수입니다.");
            }
            // userId가 현재 사용자와 일치해야 함
            if (!chatMessageDto.getUserId().equals(currentUser.getId())) {
                throw new AccessDeniedException("본인만 메시지를 전송할 수 있습니다.");
            }
        } else if ("ADMIN".equals(chatMessageDto.getSender())) {
            // ADMIN으로 발신하는 경우, 관리자 권한이 있어야 함
            if (!isAdmin) {
                throw new AccessDeniedException("관리자만 관리자 메시지를 전송할 수 있습니다.");
            }
            // adminId 필수
            if (chatMessageDto.getAdminId() == null) {
                throw new BadRequestException("관리자는 adminId가 필수입니다.");
            }
            // adminId가 현재 사용자와 일치해야 함
            if (!chatMessageDto.getAdminId().equals(currentUser.getId())) {
                throw new AccessDeniedException("본인의 관리자 ID로만 메시지를 전송할 수 있습니다.");
            }
        } else {
            throw new BadRequestException("유효하지 않은 발신자 타입입니다. USER 또는 ADMIN만 가능합니다.");
        }
    }

    /**
     * Redis에 메시지 발행
     */
    private void publishToRedis(ChatMessageDto chatMessage) {
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
