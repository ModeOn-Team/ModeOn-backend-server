package com.modeon.backend.chat.repository;

import com.modeon.backend.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
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
    
    // 최대 roomId 조회 (순차 생성 확인용)
    @Query("SELECT MAX(c.roomId) FROM ChatRoom c")
    Optional<Long> findMaxRoomId();
}
