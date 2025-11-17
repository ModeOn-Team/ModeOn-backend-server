package com.modeon.backend.repository;

import com.modeon.backend.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    Page<Comment> findByProductId(Long productId, Pageable pageable);

    @Query("SELECT COUNT(c) FROM Comment c WHERE c.product.id = :productId")
    Long countByProductId(@Param("productId") Long productId);
}