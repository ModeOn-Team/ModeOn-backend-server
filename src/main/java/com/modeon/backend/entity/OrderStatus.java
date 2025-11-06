package com.modeon.backend.entity;

import lombok.Getter;

@Getter
public enum OrderStatus {
    PENDING("결제 대기"),
    COMPLETED("결제 완료"),
    SHIPPING("배송 중"),
    DELIVERED("배송 완료"),
    CANCELLED("주문 취소");

    private final String description;

    OrderStatus(String description) {
        this.description = description;
    }
}