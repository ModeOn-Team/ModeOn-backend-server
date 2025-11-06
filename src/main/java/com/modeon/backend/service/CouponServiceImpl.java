package com.modeon.backend.service;

import com.modeon.backend.entity.Coupon;
import com.modeon.backend.entity.User;
import com.modeon.backend.repository.CouponRepository;
import com.modeon.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    // 쿠폰 받는 사람, 쿠폰 이름, 쿠폰 타입, 할인율, 쿠폰 적용 최소 구매 금액, 쿠폰 기간
    public void Coupon(Long userId, String name, String type, int value, Integer minPurchaseAmount, int durationDays) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Coupon coupon = new Coupon();
        coupon.setUser(user);
        coupon.setName(name);
        coupon.setType(type);
        coupon.setValue(value);
        coupon.setMinPurchaseAmount(minPurchaseAmount);
        coupon.setExpiresAt(LocalDateTime.now().plusDays(durationDays)); // 쿠폰 만료 시간

        couponRepository.save(coupon);
    }

    @Override
    public void WelcomeCoupons(Long userId) {
        Coupon(userId, "가입 축하 10% 할인 쿠폰", "PERCENT", 10, 10000, 7);
    }

    @Override
    public void BirthdayCoupon(Long userId) {
        Coupon(userId, "생일 축하 기념 쿠폰", "PERCENT", 15, 20000, 30);
    }

    @Override
    public void FreeShippingCoupon(Long userId, String name) {
        Coupon(userId, name, "FREE_SHIPPING", 0, 0, 30);
    }

    @Override
    public void TenPercentCoupon(Long userId) {
        Coupon(userId, "VIP 월 10% 할인 쿠폰", "PERCENT", 10, 0, 30);
    }

    @Override
    public void TwentyPercentCoupon(Long userId) {
        Coupon(userId, "VVIP 월 20% 할인 쿠폰", "PERCENT", 20, 0, 30);
    }

}
