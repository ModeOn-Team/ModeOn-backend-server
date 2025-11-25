package com.modeon.backend.review.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequest {
    private int rating;     // 별점 1~5
    private String content; // 내용
}
