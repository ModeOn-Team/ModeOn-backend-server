package com.modeon.backend.controller;

import com.modeon.backend.entity.Coupon;
import com.modeon.backend.entity.User;
import com.modeon.backend.repository.UserRepository;
import com.modeon.backend.service.CouponService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupon")
public class CouponController {

    private final CouponService couponService;
    private final UserRepository userRepository;

    @GetMapping
    public List<Coupon> getUserCoupons(@AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return couponService.getUserCoupons(user.getId());
    }

    @PostMapping("/issue")
    public String issueCoupon(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody CouponRequest request
    ) {

        String email = userDetails.getUsername();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        couponService.Coupon(
                user.getId(),
                request.getName(),
                request.getType(),
                request.getValue(),
                request.getMinPurchaseAmount(),
                request.getDurationDays()
        );

        return "쿠폰 발급 완료";
    }

    @Getter
    public static class CouponRequest {
        private String name;
        private String type;
        private int value;
        private Integer minPurchaseAmount;
        private int durationDays;
    }
}

