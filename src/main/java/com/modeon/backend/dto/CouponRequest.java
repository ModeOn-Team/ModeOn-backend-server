package com.modeon.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CouponRequest {
    private String name;
    private String type;
    private int value;
    private Integer minPurchaseAmount;
    private int durationDays;
}

