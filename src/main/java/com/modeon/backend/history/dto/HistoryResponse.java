package com.modeon.backend.history.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HistoryResponse {
    private Long id;
    private String productName;
    private String productImage;
    private int count;
    private int totalPrice;
    private String createdAt;

    private boolean hasReview;

    private Long reviewId;

}
