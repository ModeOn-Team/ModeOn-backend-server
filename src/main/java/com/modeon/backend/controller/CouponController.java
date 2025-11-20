package com.modeon.backend.controller;

import com.modeon.backend.dto.CouponResponse;
import com.modeon.backend.entity.Coupon;
import com.modeon.backend.entity.User;
import com.modeon.backend.service.CouponService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/coupon")
public class CouponController {

    private final CouponService couponService;

    @GetMapping
    public List<CouponResponse> getUserCoupons(@AuthenticationPrincipal User user) {

        List<Coupon> coupons = couponService.getUserCoupons(user.getId());

        return coupons.stream()
                .map(coupon -> new CouponResponse(
                        coupon.getCouponId(),
                        coupon.getName(),
                        coupon.getType(),
                        coupon.getValue(),
                        coupon.getMinPurchaseAmount(),
                        coupon.isUsed(),
                        coupon.getExpiresAt()
                ))
                .collect(Collectors.toList());
    }

    @PostMapping("/issue")
    public String issueCoupon(
            @AuthenticationPrincipal User user,
            @RequestBody CouponRequest request
    ) {

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

