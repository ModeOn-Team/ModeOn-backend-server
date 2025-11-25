package com.modeon.backend.review.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReviewResponse {
    private Long id;
    private Long historyId;
    private Long productId;
    private String productName;
    private String userName;
    private int rating;
    private String content;
    private String createdAt;

    private List<String> imageUrls;
}
