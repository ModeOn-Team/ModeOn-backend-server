package com.modeon.backend.service;

public interface CouponService {

    void Coupon(Long userId, String name, String type, int value, Integer minPurchaseAmount, int durationDays);

    void WelcomeCoupons(Long userId);

    void BirthdayCoupon(Long userId);

    void FreeShippingCoupon(Long userId, String name);

    void TenPercentCoupon(Long userId);

    void TwentyPercentCoupon(Long userId);
}
