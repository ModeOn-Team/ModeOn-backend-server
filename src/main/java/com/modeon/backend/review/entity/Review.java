package com.modeon.backend.review.entity;

import com.modeon.backend.entity.User;
import com.modeon.backend.entity.Product;
import com.modeon.backend.history.entity.History;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @OneToOne(fetch = FetchType.LAZY)
    private History history;

    private int rating;
    private String content;

    private String imageUrl; 

    private LocalDateTime createdAt;
}
