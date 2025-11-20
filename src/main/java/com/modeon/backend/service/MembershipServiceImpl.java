package com.modeon.backend.service;

import com.modeon.backend.entity.MembershipLevel;
import com.modeon.backend.entity.User;
import com.modeon.backend.history.repository.HistoryRepository;
import com.modeon.backend.repository.OrderRepository;
import com.modeon.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class MembershipServiceImpl implements MembershipService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final CouponService couponService;
    private final HistoryRepository historyRepository;

    private static final int PERIOD_MONTHS = 12; // 멤버쉽 등급 기준 기간

    @Transactional
    @Override
    public void membershipUpgrade(Long userId, int latestOrderAmount) {
        log.info("=== 멤버십 업그레이드 시작: userId={}, latestOrderAmount={}", userId, latestOrderAmount);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. (ID: " + userId + ")"));

        // 누적 구매액 확인
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusMonths(PERIOD_MONTHS);

        Long totalAmountLong = historyRepository.calculateTotalPurchaseAmount(userId, startDate, endDate);
        int totalAmount = totalAmountLong != null ? totalAmountLong.intValue() : 0;

        log.info("누적 구매액: totalAmount={}", totalAmount);

        // 새로운 등급을 부여받을지 판단
        MembershipLevel newLevel = MembershipLevel.getLevelByAmount(totalAmount);
        MembershipLevel oldLevel = user.getMembership();

        log.info("등급 비교: oldLevel={}, newLevel={}", oldLevel, newLevel);

        // 등급 변경이 필요한지 확인
        if (newLevel.compareTo(oldLevel) > 0) {
            log.info("등급 업그레이드 진행: {} -> {}", oldLevel, newLevel);

            // 등급 업데이트
            user.setMembership(newLevel);
            userRepository.save(user);

            // 등급 상승시 등급에 맞는 추가 혜택 지급
            grantUpgradeBenefits(userId, oldLevel, newLevel);
        } else {
            log.info("등급 변경 없음");
        }
    }

    private void grantUpgradeBenefits(Long userId, MembershipLevel oldLevel, MembershipLevel newLevel) {
        log.info("=== 등급 상승 혜택 지급 시작: userId={}, oldLevel={}, newLevel={}", userId, oldLevel, newLevel);

        try {
            // SILVER -> GOLD 등급 상승 시 지급
            if (newLevel == MembershipLevel.GOLD && oldLevel.compareTo(MembershipLevel.GOLD) < 0) {
                log.info("GOLD 등급 승격 쿠폰 발급 시작");
                couponService.FreeShippingCoupon(userId, "GOLD 등급 승격 기념 무료배송");
                log.info("GOLD 등급 승격 쿠폰 발급 완료");
            }

            // GOLD -> VIP 등급 상승 시 지급
            if (newLevel == MembershipLevel.VIP && oldLevel.compareTo(MembershipLevel.VIP) < 0) {
                log.info("VIP 등급 승격 쿠폰 발급 시작");
                couponService.TenPercentCoupon(userId);
                log.info("VIP 등급 승격 쿠폰 발급 완료");
            }

            // VIP -> VVIP 등급 상승 시 지급
            if (newLevel == MembershipLevel.VVIP && oldLevel.compareTo(MembershipLevel.VVIP) < 0) {
                log.info("VVIP 등급 승격 쿠폰 발급 시작");
                couponService.TwentyPercentCoupon(userId);
                log.info("VVIP 등급 승격 쿠폰 발급 완료");
            }
        } catch (Exception e) {
            log.error("쿠폰 발급 중 오류 발생", e);
            throw e;
        }
    }

    @Transactional
    @Override
    public void welcomeCoupon(Long userId) {
        couponService.WelcomeCoupons(userId);
    }

    @Override
    @Scheduled(cron = "0 0 0 1 * *") // 매달 1일 자정(00시 00분 00초)에 실행
    public void periodicCoupons() {

        userRepository.findAll().forEach(user -> {
            MembershipLevel currentLevel = user.getMembership();
            Long userId = user.getId();

            // 1. SILVER 이상: 생일 쿠폰 - 생일 달에만 지급
            if (currentLevel.compareTo(MembershipLevel.SILVER) >= 0 && isBirthdayMonth(user)) {
                couponService.BirthdayCoupon(userId);
            }

            // 2. GOLD 이상: 무료배송 쿠폰
            if (currentLevel.compareTo(MembershipLevel.GOLD) >= 0) {
                couponService.FreeShippingCoupon(userId, "정기 무료배송 쿠폰");
            }

            // 3. VIP 이상: 월 1회 10% 할인 쿠폰
            if (currentLevel.compareTo(MembershipLevel.VIP) >= 0) {
                couponService.TenPercentCoupon(userId);
            }

            // 4. VVIP 이상: 월 1회 20% 할인 쿠폰
            if (currentLevel.compareTo(MembershipLevel.VVIP) >= 0) {
                couponService.TwentyPercentCoupon(userId);
            }
        });
    }

    private boolean isBirthdayMonth(User user) {

        LocalDate birthMonthDate = user.getBirthMonth();

        if (birthMonthDate == null) {

            return false;
        }

        int currentMonth = LocalDate.now().getMonthValue();

        int birthMonth = birthMonthDate.getMonthValue();

        return currentMonth == birthMonth;
    }
}