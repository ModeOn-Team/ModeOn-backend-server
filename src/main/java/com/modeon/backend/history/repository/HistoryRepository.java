package com.modeon.backend.history.repository;

import com.modeon.backend.history.entity.History;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface HistoryRepository extends JpaRepository<History, Long> {


    List<History> findByUser_IdOrderByCreatedAtDesc(Long userId);

    List<History> findByRequestStatusIn(List<String> statuses);

    @Query("SELECT COALESCE(SUM(h.totalPrice), 0) FROM History h " +
            "WHERE h.user.id = :userId " +
            "AND h.createdAt BETWEEN :startDate AND :endDate")
    Long calculateTotalPurchaseAmount(
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );


}

