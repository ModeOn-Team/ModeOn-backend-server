package com.modeon.backend.history.repository;

import com.modeon.backend.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {
    List<History> findByUserId(Long userId);

    List<History> findByUserIdOrderByCreatedAtDesc(Long userId);
}

