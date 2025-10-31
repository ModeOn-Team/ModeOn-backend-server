package com.modeon.backend.chat.repository;

import com.modeon.backend.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    
    List<ChatMessage> findByRoomIdOrderByCreatedAtAsc(Long roomId);
    
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.roomId = :roomId ORDER BY cm.createdAt DESC")
    List<ChatMessage> findLatestMessagesByRoomId(@Param("roomId") Long roomId);
    
    List<ChatMessage> findByUserId(Long userId);
    
    List<ChatMessage> findByAdminId(Long adminId);
}
