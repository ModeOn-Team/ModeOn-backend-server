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

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByOrderByPriceAsc();

    List<Product> findAllByOrderByPriceDesc();

    @Query("""
            SELECT DISTINCT p FROM Product p
            JOIN p.variants v
            JOIN p.category c
            WHERE (:gender IS NULL OR p.gender = :gender)
              AND (:size IS NULL OR v.size = :size)
              AND (:color IS NULL OR v.color = :color)
              AND (
                  :categoryId IS NULL OR
                  c.id = :categoryId OR
                  (c.parent IS NOT NULL AND c.parent.id = :categoryId) OR
                  (c.parent.parent IS NOT NULL AND c.parent.parent.id = :categoryId)
              )
              AND (:word IS NULL OR :word = '' OR p.name LIKE CONCAT('%', :word, '%'))
            """)
    Page<Product> searchProducts(
            @Param("gender") Gender gender,
            @Param("categoryId") Long categoryId,
            @Param("size") Size size,
            @Param("color") Color color,
            @Param("word") String word,
            Pageable pageable
    );
}
