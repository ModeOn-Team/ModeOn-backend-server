package com.modeon.backend.cart.repository;


import com.modeon.backend.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart,Long> {
    List<Cart> findByUserId(Long userId);
    Optional<Cart> findByUserIdAndProductId(Long userId, Long productId);
    void deleteByUserIdAndProductId(Long userId, Long productId);


    void deleteByUserId(Long userId);


    int deleteByIdAndUserId(Long id, Long userId);

}
