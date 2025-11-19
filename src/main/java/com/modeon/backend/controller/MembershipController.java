package com.modeon.backend.controller;

import com.modeon.backend.entity.User;
import com.modeon.backend.history.repository.HistoryRepository;
import com.modeon.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/membership")
@RequiredArgsConstructor
public class MembershipController {

    private final UserRepository userRepository;
    private final HistoryRepository historyRepository;

    @GetMapping
    public MembershipResponse getMembership(@AuthenticationPrincipal User user) {

        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(12);

        Long totalSpent = historyRepository.calculateTotalPurchaseAmount(
                user.getId(), startDate, endDate
        );

        return new MembershipResponse(
                user.getMembership().name(),
                totalSpent != null ? totalSpent : 0,
                user.getUpdatedAt(),
                user.getPoint(),
                0, // 쿠폰 개수 (추가 가능)
                0  // 리뷰 개수 (추가 가능)
        );
    }

    record MembershipResponse(
            String level,
            Long totalSpent,
            LocalDateTime lastUpdated,
            int points,
            int couponCount,
            int reviewCount
    ) {}
}
