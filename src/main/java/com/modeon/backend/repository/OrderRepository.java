package com.modeon.backend.repository;

import com.modeon.backend.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT COALESCE(SUM(o.totalPrice), 0) FROM Order o " +
            "WHERE o.user.id = :userId " +
            "AND o.orderDate BETWEEN :startDate AND :endDate " +
            "AND o.status = 'COMPLETED'")

    Long calculateTotalPurchaseAmount( // 총 결제 누적 금액
            @Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
