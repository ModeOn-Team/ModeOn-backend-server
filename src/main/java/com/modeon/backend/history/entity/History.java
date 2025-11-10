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
}
