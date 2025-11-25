package com.modeon.backend.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "room_id", nullable = false)
    private Long roomId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "admin_id")
    private Long adminId;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(name = "metadata", columnDefinition = "TEXT")
    private String metadata;  // JSON String 형태로 메타데이터 저장 (ERD의 Field2)

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum MessageType {
        TEXT, IMAGE, FILE, SYSTEM, EMOJI, REPLY
    }

    @Builder
    public ChatMessage(Long roomId, Long userId, Long adminId, String content, MessageType messageType, String metadata) {
        this.roomId = roomId;
        this.userId = userId;
        this.adminId = adminId;
        this.content = content;
        this.messageType = messageType;
        this.metadata = metadata;
    }

    public String getSender() {
        if (userId != null) {
            return "USER";
        } else if (adminId != null) {
            return "ADMIN";
        }
        return "SYSTEM";
    }
}
