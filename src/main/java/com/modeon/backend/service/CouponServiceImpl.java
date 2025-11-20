package com.modeon.backend.service;

import com.modeon.backend.entity.Coupon;
import com.modeon.backend.entity.User;
import com.modeon.backend.repository.CouponRepository;
import com.modeon.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public void Coupon(Long userId, String name, String type,
                       int value, Integer minPurchaseAmount, int durationDays) {

        log.info("=== 쿠폰 발급 시작: userId={}, name={}, type={}, value={}", userId, name, type, value);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        Coupon coupon = new Coupon();
        coupon.setUser(user);
        coupon.setName(name);
        coupon.setType(type);
        coupon.setValue(value);
        coupon.setMinPurchaseAmount(minPurchaseAmount);
        coupon.setExpiresAt(LocalDateTime.now().plusDays(durationDays));

        couponRepository.save(coupon);
        log.info("쿠폰 발급 완료: userId={}, name={}, couponId={}", userId, name, coupon.getCouponId());
    }

    @Override
    public List<Coupon> getUserCoupons(Long userId) {
        return couponRepository.findByUserId(userId);
    }

    @Transactional
    @Override
    public void WelcomeCoupons(Long userId) {
        log.info("웰컴 쿠폰 발급: userId={}", userId);
        Coupon(userId, "가입 축하 10% 할인 쿠폰", "PERCENT", 10, 10000, 7);
    }

    @Transactional
    @Override
    public void BirthdayCoupon(Long userId) {
        log.info("생일 쿠폰 발급: userId={}", userId);
        Coupon(userId, "생일 축하 쿠폰", "PERCENT", 15, 20000, 30);
    }

    @Transactional
    @Override
    public void FreeShippingCoupon(Long userId, String name) {
        log.info("무료배송 쿠폰 발급: userId={}, name={}", userId, name);
        Coupon(userId, name, "FREE_SHIPPING", 0, 0, 30);
    }

    @Transactional
    @Override
    public void TenPercentCoupon(Long userId) {
        log.info("VIP 10% 할인 쿠폰 발급: userId={}", userId);
        Coupon(userId, "VIP 월 10% 할인 쿠폰", "PERCENT", 10, 0, 30);
    }

    @Transactional
    @Override
    public void TwentyPercentCoupon(Long userId) {
        log.info("VVIP 20% 할인 쿠폰 발급: userId={}", userId);
        Coupon(userId, "VVIP 월 20% 할인 쿠폰", "PERCENT", 20, 0, 30);
    }
}
