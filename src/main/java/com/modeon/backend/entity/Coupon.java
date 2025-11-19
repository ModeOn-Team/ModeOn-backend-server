package com.modeon.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "Coupon")
@Getter
@Setter
public class Coupon {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "coupon_id")
    private Long couponId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "value", nullable = false)
    private int value;

    @Column(name = "min_purchase_amount")
    private Integer minPurchaseAmount;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;

    @Column(name = "issued_at", updatable = false)
    private LocalDateTime issuedAt = LocalDateTime.now();

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}

