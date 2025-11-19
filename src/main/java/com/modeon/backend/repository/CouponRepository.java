package com.modeon.backend.repository;

import com.modeon.backend.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CouponRepository extends JpaRepository<Coupon, Long> {

    List<Coupon> findByUserId(Long userId);

    Optional<Coupon> findFirstByUserIdAndNameAndIssuedAtBetween(
            Long userId,
            String name,
            LocalDateTime startDate,
            LocalDateTime endDate
    );
}

