package com.modeon.backend.chat.repository;

import com.modeon.backend.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    // 가장 최근에 생성된 활성 채팅방 1개만 반환
    Optional<ChatRoom> findFirstByUserIdAndIsActiveTrueOrderByCreatedAtDesc(Long userId);
    
    List<ChatRoom> findByIsActiveTrue();
    
    List<ChatRoom> findByAdminId(Long adminId);
    
    Optional<ChatRoom> findByRoomIdAndIsActiveTrue(Long roomId);
}
