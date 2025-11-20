package com.modeon.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class CouponResponse {
    private Long couponId;
    private String name;
    private String type;
    private int value;
    private Integer minPurchaseAmount;
    private boolean isUsed;
    private LocalDateTime expiresAt;
}
