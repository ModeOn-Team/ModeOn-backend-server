package com.modeon.backend.review.repository;



import com.modeon.backend.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {


    boolean existsByHistory_Id(Long historyId);


    Optional<Review> findByHistory_Id(Long historyId);

    List<Review> findByProduct_IdOrderByCreatedAtDesc(Long productId);
}
