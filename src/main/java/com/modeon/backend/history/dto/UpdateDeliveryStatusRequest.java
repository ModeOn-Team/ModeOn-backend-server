package com.modeon.backend.history.dto;

import lombok.Getter;

@Getter
public class UpdateDeliveryStatusRequest {
    // PREPARING / SHIPPING / DELIVERED
    private String status;

    // 새로운 운송장 번호
    private String trackingNumber;

    // 택배사
    private String courierCode;
}
