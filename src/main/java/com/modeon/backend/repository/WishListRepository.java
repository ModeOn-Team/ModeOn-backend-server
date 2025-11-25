package com.modeon.backend.repository;

import com.modeon.backend.entity.Product;
import com.modeon.backend.entity.User;
import com.modeon.backend.entity.WishList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WishListRepository extends JpaRepository<WishList, Long> {
    boolean existsByUserAndProduct(User user, Product product);

    long countByProductId(Long productId);
    void deleteByUserAndProduct(User user, Product product);

    @Query("SELECT w.product FROM WishList w WHERE w.user.id = :userId")
    Page<Product> findProductsByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT COUNT(w) FROM WishList  w WHERE w.user.id = :userId")
    Long countByUserId(@Param("userId") Long userId);
}
