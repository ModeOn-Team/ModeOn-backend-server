package com.modeon.backend.repository;

import com.modeon.backend.entity.Color;
import com.modeon.backend.entity.Gender;
import com.modeon.backend.entity.Product;
import com.modeon.backend.entity.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    @Query("""
    SELECT DISTINCT p FROM Product p
    JOIN p.category c
    JOIN p.variants v
    WHERE (:gender IS NULL OR p.gender = :gender)
      AND (
            c.id = :categoryId
            OR (c.parent IS NOT NULL AND c.parent.id = :categoryId)
            OR (c.parent.parent IS NOT NULL AND c.parent.parent.id = :categoryId)
      )
      AND (:size IS NULL OR v.size = :size)
      AND (:color IS NULL OR v.color = :color)
    """)
    Page<Product> searchByCategoryTree(
            @Param("gender") Gender gender,
            @Param("categoryId") Long categoryId,
            @Param("size") Size size,
            @Param("color") Color color,
            @Param("word") String word,
            Pageable pageable
    );


    @Query("""
    SELECT DISTINCT p FROM Product p
    JOIN p.variants v
    JOIN p.category c
    WHERE (:gender IS NULL OR p.gender = :gender)
      AND (:size IS NULL OR v.size = :size)
      AND (:color IS NULL OR v.color = :color)
    """)
    Page<Product> searchWithoutCategory(
            @Param("gender") Gender gender,
            @Param("size") Size size,
            @Param("color") Color color,
            @Param("word") String word,
            Pageable pageable
    );
}