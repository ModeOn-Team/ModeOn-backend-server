package com.modeon.backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


import java.time.LocalDateTime;

@Entity
@Table(name = "History")
@Getter
@Setter
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "history_id")
    private Long historyId; // 주문한 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 주문한 사용자

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice; // 총 결제 금액

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now(); // 주문 날짜

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status; // 주문 상태
}
