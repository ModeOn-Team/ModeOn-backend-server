package com.modeon.backend.history.entity;

import com.modeon.backend.entity.User;
import com.modeon.backend.entity.Product;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    private int price;
    private int totalPrice;
    private String size;
    private String color;

    private LocalDateTime createdAt;

    @Builder.Default
    @Column(nullable = false)
    private String status = "PAID";

    private String trackingNumber;
    private String courierCode;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;

    private String requestStatus;
    private String requestReason;
    private String adminResponseReason;

    @ElementCollection
    @CollectionTable(name = "history_request_images", joinColumns = @JoinColumn(name = "history_id"))
    @Column(name = "image_url")
    private List<String> requestImages = new ArrayList<>();


    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
