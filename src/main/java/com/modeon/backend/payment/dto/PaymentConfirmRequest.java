package com.modeon.backend.payment.dto;

import lombok.Getter;

@Getter
public class PaymentConfirmRequest {
    private String paymentKey;
    private String orderId;
    private int amount;
}
