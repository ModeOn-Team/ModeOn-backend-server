package com.modeon.backend.entity;

import lombok.Getter;

@Getter
public enum MembershipLevel {

    // 멤버쉽 등급 이름, 등급 상승을 위한 금액, 포인트 적립률
    WELCOME("WELCOME", 0, 0.0),
    SILVER("SILVER", 100000, 0.02),
    GOLD("GOLD", 300000, 0.05),
    VIP("VIP", 500000, 0.07),
    VVIP("VVIP", 1000000, 0.10);

    private final String levelName;
    private final int minimumAmount;
    private final double accrualRate;

    MembershipLevel(String levelName, int minimumAmount, double accrualRate) {
        this.levelName = levelName;
        this.minimumAmount = minimumAmount;
        this.accrualRate = accrualRate;
    }

    // 누적 구매 금액을 확인해 멤버쉽 등급을 부여
    public static MembershipLevel getLevelByAmount(int currentAmount) {
        for (MembershipLevel level : values()) {
            if (currentAmount >= level.minimumAmount) {
                return level;
            }
        }
        return WELCOME;
    }
}
