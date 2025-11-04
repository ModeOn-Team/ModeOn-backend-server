package com.modeon.backend.repository;

import com.modeon.backend.entity.Color;
import com.modeon.backend.entity.ProductVariant;
import com.modeon.backend.entity.Size;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductId(Long productId);
    Optional<ProductVariant> findByProductIdAndColorAndSize(Long productId, Color color, Size size);
}
