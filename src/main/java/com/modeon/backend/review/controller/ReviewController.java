package com.modeon.backend.review.controller;

import com.modeon.backend.dto.ProductResponse;
import com.modeon.backend.entity.User;
import com.modeon.backend.review.dto.ReviewRequest;
import com.modeon.backend.review.dto.ReviewResponse;
import com.modeon.backend.review.entity.Review;
import com.modeon.backend.review.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/{reviewId}")
    public ReviewResponse getReviewById(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId
    ) {
        return reviewService.getReviewById(user, reviewId);
    }

    @GetMapping("/review-list")
    public ResponseEntity<Page<ReviewResponse>> getAllReviews(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ReviewResponse> reviews = reviewService.getAllReviews(pageable);
        return ResponseEntity.ok(reviews);
    }

    @PostMapping("/{historyId}")
    public ResponseEntity<String> writeReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long historyId,
            @RequestBody ReviewRequest request
    ) {
        reviewService.writeReview(user, historyId, request);
        return ResponseEntity.ok("리뷰가 작성되었습니다.");
    }



    @GetMapping("/history/{historyId}")
    public ReviewResponse getReviewByHistory(
            @AuthenticationPrincipal User user,
            @PathVariable Long historyId
    ) {
        return reviewService.getReviewByHistory(user, historyId);
    }

    @GetMapping("/product/{productId}")
    public List<ReviewResponse> getReviewsByProduct(@PathVariable Long productId) {
        return reviewService.getReviewsByProduct(productId);
    }

    // 수정
    @PutMapping("/{reviewId}")
    public ResponseEntity<String> updateReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId,
            @RequestBody ReviewRequest request
    ) {
        reviewService.updateReview(user, reviewId, request);
        return ResponseEntity.ok("리뷰가 수정되었습니다.");
    }

    //  삭제
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<String> deleteReview(
            @AuthenticationPrincipal User user,
            @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(user, reviewId);
        return ResponseEntity.ok("리뷰가 삭제되었습니다.");
    }

}
