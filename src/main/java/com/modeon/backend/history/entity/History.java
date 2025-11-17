package com.modeon.backend.history.entity;

import com.modeon.backend.entity.User;
import com.modeon.backend.entity.Product;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class History {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    private int count;
    private int price;       // 개당 가격
    private int totalPrice;  // 총 가격

    private LocalDateTime createdAt;



    @Builder.Default
    @Column(nullable = false)
    private String status = "PAID";


    // 운송장 번호
    private String trackingNumber;

    // 택배사 코드
    private String courierCode;

    // 배송 시작 시간
    private LocalDateTime shippedAt;

    // 배송 완료 시간
    private LocalDateTime deliveredAt;

    // 교환/환불 요청 상태 (null, REFUND_REQUEST, EXCHANGE_REQUEST)
    private String requestStatus;

    // 교환/환불 사유
    private String requestReason;

    private String adminResponseReason;


}
