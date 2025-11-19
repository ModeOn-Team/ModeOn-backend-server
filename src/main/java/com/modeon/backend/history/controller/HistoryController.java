package com.modeon.backend.history.controller;

import com.modeon.backend.entity.User;
import com.modeon.backend.history.dto.ExchangeRequest;
import com.modeon.backend.history.dto.HistoryResponse;
import com.modeon.backend.history.dto.RefundRequest;
import com.modeon.backend.history.entity.History;
import com.modeon.backend.history.repository.HistoryRepository;
import com.modeon.backend.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/history")
public class HistoryController {
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy.MM.dd HH:mm");

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
                            .shippedAt(h.getShippedAt() != null ? h.getShippedAt().format(dtf) : null)
                            .deliveredAt(h.getDeliveredAt() != null ? h.getDeliveredAt().format(dtf) : null)
                            .productId(h.getProduct().getId())
                            .requestStatus(h.getRequestStatus())
                            .adminResponseReason(h.getAdminResponseReason())
                            .size(h.getSize())
                            .color(h.getColor())
                            .build();

                })
                .toList();
    }

    @GetMapping("/{historyId}")
    public ResponseEntity<?> getHistoryDetail(
            @AuthenticationPrincipal User user,
            @PathVariable Long historyId
    ) {
        var h = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("구매 기록을 찾을 수 없습니다."));

        if (!Objects.equals(h.getUser().getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("본인의 구매내역만 조회/요청할 수 있습니다.");
        }

        String imageUrl = null;
        if (h.getProduct().getDetailImages() != null && !h.getProduct().getDetailImages().isEmpty()) {
            imageUrl = h.getProduct().getDetailImages().get(0).getImageUrl();
        }

        return ResponseEntity.ok(
                HistoryResponse.builder()
                        .id(h.getId())
                        .productName(h.getProduct().getName())
                        .productImage(imageUrl)
                        .count(h.getCount())
                        .totalPrice(h.getTotalPrice())
                        .createdAt(h.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                        .status(h.getStatus())
                        .trackingNumber(h.getTrackingNumber())
                        .courierCode(h.getCourierCode())
                        .shippedAt(h.getShippedAt() != null ? h.getShippedAt().format(dtf) : null)
                        .deliveredAt(h.getDeliveredAt() != null ? h.getDeliveredAt().format(dtf) : null)
                        .size(h.getSize())
                        .color(h.getColor())
                        .requestStatus(h.getRequestStatus())
                        .productId(h.getProduct().getId())
                        .adminResponseReason(h.getAdminResponseReason())
                        .build()
        );
    }



    /* 환불 요청 */
    @PostMapping("/{historyId}/refund")
    public ResponseEntity<String> requestRefund(
            @AuthenticationPrincipal User user,
            @PathVariable Long historyId,
            @RequestPart("reason") String reason,
            @RequestPart(value = "images", required = false) List<MultipartFile> images   // 🔥 추가됨
    ) {
        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("구매 기록을 찾을 수 없습니다."));

        if (!Objects.equals(h.getUser().getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("본인의 구매내역만 조회/요청할 수 있습니다.");
        }

        if (!h.getStatus().equals("DELIVERED")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("배송 완료된 상태에서만 환불이 가능합니다.");
        }

        h.setRequestStatus("REFUND_REQUEST");
        h.setRequestReason(reason);


        historyRepository.save(h);

        return ResponseEntity.ok("환불 요청이 완료되었습니다.");
    }


    /* 교환 요청 */
    @PostMapping("/{historyId}/exchange")
    public ResponseEntity<String> requestExchange(
            @AuthenticationPrincipal User user,
            @PathVariable Long historyId,
            @RequestPart("reason") String reason,
            @RequestPart(value = "images", required = false) List<MultipartFile> images  // 🔥 추가됨
    ) {
        History h = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("구매 기록을 찾을 수 없습니다."));

        if (!Objects.equals(h.getUser().getId(), user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("본인의 구매내역만 조회/요청할 수 있습니다.");
        }

        if (!h.getStatus().equals("DELIVERED")) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("배송 완료된 상태에서만 교환이 가능합니다.");
        }

        h.setRequestStatus("EXCHANGE_REQUEST");
        h.setRequestReason(reason);



        historyRepository.save(h);

        return ResponseEntity.ok("교환 요청이 완료되었습니다.");
    }
}
