package com.modeon.backend.chat.repository;

import com.modeon.backend.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    
    Optional<ChatRoom> findByUserIdAndIsActiveTrue(Long userId);
    
    List<ChatRoom> findByIsActiveTrue();
    
    List<ChatRoom> findByAdminId(Long adminId);
    
    Optional<ChatRoom> findByRoomIdAndIsActiveTrue(Long roomId);
}
