package com.modeon.backend.review.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ReviewRequest {
    private int rating;
    private String content;

    // 프론트에서 보내주는 남길 이미지
    private List<String> imageUrls;
}
