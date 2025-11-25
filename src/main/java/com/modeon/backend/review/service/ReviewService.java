package com.modeon.backend.review.service;

import com.modeon.backend.entity.User;
import com.modeon.backend.history.entity.History;
import com.modeon.backend.history.repository.HistoryRepository;
import com.modeon.backend.review.dto.ReviewRequest;
import com.modeon.backend.review.dto.ReviewResponse;
import com.modeon.backend.review.entity.Review;
import com.modeon.backend.review.repository.ReviewRepository;
import com.modeon.backend.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final HistoryRepository historyRepository;
    private final FileUploadService fileUploadService;

    @Transactional(readOnly = true)
    public ReviewResponse getReviewById(User user, Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        if (!Objects.equals(review.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("본인의 리뷰만 조회할 수 있습니다.");
        }

        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> getAllReviews(Pageable pageable) {
        Page<Review> reviews = reviewRepository.findAll(pageable);
        return reviews.map(this::toResponse);
    }

    public void writeReview(User user, Long historyId, ReviewRequest request, List<MultipartFile> images) {

        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("구매 내역을 찾을 수 없습니다."));

        if (!Objects.equals(history.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("본인의 구매내역에만 리뷰를 작성할 수 있습니다.");
        }

        if (reviewRepository.existsByHistory_Id(historyId)) {
            throw new IllegalArgumentException("이미 이 구매내역에 대한 리뷰가 존재합니다.");
        }

        List<String> imageUrls = uploadImages(images);

        Review review = Review.builder()
                .user(user)
                .product(history.getProduct())
                .history(history)
                .rating(request.getRating())
                .content(request.getContent())
                .imageUrls(imageUrls)
                .createdAt(LocalDateTime.now())
                .build();

        reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public ReviewResponse getReviewByHistory(User user, Long historyId) {

        History history = historyRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("구매 내역을 찾을 수 없습니다."));

        if (!Objects.equals(history.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("본인의 구매내역에만 리뷰를 조회할 수 있습니다.");
        }

        Review review = reviewRepository.findByHistory_Id(historyId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰가 존재하지 않습니다."));

        return toResponse(review);
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> getReviewsByProduct(Long productId) {
        return reviewRepository.findByProduct_IdOrderByCreatedAtDesc(productId)
                .stream()
                .map(this::toResponse)
                .toList();
    }


    public void updateReview(User user, Long reviewId, ReviewRequest request, List<MultipartFile> images) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!Objects.equals(review.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("본인의 리뷰만 수정할 수 있습니다.");
        }

        review.setRating(request.getRating());
        review.setContent(request.getContent());

        List<String> finalUrls = new ArrayList<>();

        // 기존 이미지들
        if (request.getImageUrls() != null) {
            finalUrls.addAll(request.getImageUrls());
        }

        //  새로 올린 이미지
        if (images != null) {
            for (MultipartFile f : images) {
                if (!f.isEmpty()) {
                    String url = fileUploadService.uploadOriginal(f, "reviews");
                    finalUrls.add(url);
                }
            }
        }

        review.setImageUrls(finalUrls);
    }



    public void deleteReview(User user, Long reviewId) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

        if (!Objects.equals(review.getUser().getId(), user.getId())) {
            throw new IllegalArgumentException("본인의 리뷰만 삭제할 수 있습니다.");
        }

        reviewRepository.delete(review);
    }

    private List<String> uploadImages(List<MultipartFile> files) {
        List<String> urls = new ArrayList<>();
        if (files != null) {
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    String url = fileUploadService.uploadOriginal(file, "reviews");
                    urls.add(url);
                }
            }
        }
        return urls;
    }

    private ReviewResponse toResponse(Review r) {
        return ReviewResponse.builder()
                .id(r.getId())
                .historyId(r.getHistory().getId())
                .productId(r.getProduct().getId())
                .productName(r.getProduct().getName())
                .userName(r.getUser().getUsername())
                .rating(r.getRating())
                .content(r.getContent())
                .imageUrls(r.getImageUrls())
                .createdAt(r.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")))
                .build();
    }
}
