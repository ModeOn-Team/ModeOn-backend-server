package com.modeon.backend.history.controller;

import com.modeon.backend.entity.User;
import com.modeon.backend.history.dto.HistoryResponse;
import com.modeon.backend.history.repository.HistoryRepository;
import com.modeon.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryRepository historyRepository;
    private final ReviewRepository reviewRepository;

    @GetMapping
    public List<HistoryResponse> getMyHistoryList(@AuthenticationPrincipal User user) {

        return historyRepository.findByUser_IdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(h -> {
                    String imageUrl = null;
                    if (h.getProduct().getDetailImages() != null && !h.getProduct().getDetailImages().isEmpty()) {
                        imageUrl = h.getProduct().getDetailImages().get(0).getImageUrl();
                    }

                    boolean hasReview = reviewRepository.existsByHistory_Id(h.getId());

                    Long reviewId = null;
                    if (hasReview) {
                        reviewId = reviewRepository.findByHistory_Id(h.getId())
                                .map(r -> r.getId())
                                .orElse(null);
                    }

                    return HistoryResponse.builder()
                            .id(h.getId())
                            .productName(h.getProduct().getName())
                            .productImage(imageUrl)
                            .count(h.getCount())
                            .totalPrice(h.getTotalPrice())
                            .createdAt(h.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                            .hasReview(hasReview)
                            .reviewId(reviewId)
                            .status(h.getStatus())
                            .trackingNumber(h.getTrackingNumber())
                            .courierCode(h.getCourierCode())
                            .shippedAt(h.getShippedAt() != null ? h.getShippedAt().toString() : null)
                            .deliveredAt(h.getDeliveredAt() != null ? h.getDeliveredAt().toString() : null)
                            .productId(h.getProduct().getId())

                            .build();

                })
                .toList();
    }

    @GetMapping("/{historyId}")
    public HistoryResponse getHistoryDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long historyId
    ) {
        var h = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("구매 기록을 찾을 수 없습니다."));


        if (h.getUser().getId() != user.getId()) {
            throw new IllegalArgumentException("본인의 구매내역만 조회할 수 있습니다.");
        }

        String imageUrl = null;
        if (h.getProduct().getDetailImages() != null && !h.getProduct().getDetailImages().isEmpty()) {
            imageUrl = h.getProduct().getDetailImages().get(0).getImageUrl();
        }

        return HistoryResponse.builder()
                .id(h.getId())
                .productName(h.getProduct().getName())
                .productImage(imageUrl)
                .count(h.getCount())
                .totalPrice(h.getTotalPrice())
                .createdAt(h.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .status(h.getStatus())
                .trackingNumber(h.getTrackingNumber())
                .courierCode(h.getCourierCode())
                .shippedAt(h.getShippedAt() != null ? h.getShippedAt().toString() : null)
                .deliveredAt(h.getDeliveredAt() != null ? h.getDeliveredAt().toString() : null)
                .productId(h.getProduct().getId())

                .build();
    }
}
