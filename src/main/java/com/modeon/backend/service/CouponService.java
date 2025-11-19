package com.modeon.backend.service;

import com.modeon.backend.entity.Coupon;
import java.util.List;

public interface CouponService {

    void Coupon(Long userId, String name, String type, int value, Integer minPurchaseAmount, int durationDays);

    List<Coupon> getUserCoupons(Long userId);

    void WelcomeCoupons(Long userId);

    void BirthdayCoupon(Long userId);

    void FreeShippingCoupon(Long userId, String name);

    void TenPercentCoupon(Long userId);

    void TwentyPercentCoupon(Long userId);
}
