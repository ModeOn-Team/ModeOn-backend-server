package com.modeon.backend.service;

public interface MembershipService {
    void membershipUpgrade(Long userId, int latestOrderAmount); // 멤버쉽 등급 상승
    void welcomeCoupon(Long userId); // 신규 가입 쿠폰
    void periodicCoupons(); // 매달 1일 지급되는 쿠폰
}
