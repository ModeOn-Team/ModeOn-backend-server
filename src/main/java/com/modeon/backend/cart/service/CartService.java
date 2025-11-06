package com.modeon.backend.cart.service;

import com.modeon.backend.cart.dto.CartItemRequest;
import com.modeon.backend.cart.entity.Cart;
import com.modeon.backend.cart.repository.CartRepository;
import com.modeon.backend.entity.User;
import com.modeon.backend.product.entity.Product;
import com.modeon.backend.product.repository.ProductRepository;
import com.modeon.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public void addItem(Long userId, CartItemRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다."));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 상품입니다."));

        // 기존 장바구니에 같은 상품이 있는지 확인
        Cart existingCart = cartRepository.findByUserIdAndProductId(userId, request.getProductId())
                .orElse(null);

        if (existingCart == null) {
            // 새 상품 추가
            Cart newCart = Cart.builder()
                    .user(user)
                    .product(product)
                    .count(request.getCount())
                    .build();
            cartRepository.save(newCart);
        } else {
            // 기존 상품 수량 누적
            existingCart.setCount(existingCart.getCount() + request.getCount());
            cartRepository.save(existingCart);
        }
    }

    public List<Cart> getCartItems(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    public void updateCount(Long userId, Long productId, int count) {
        Cart cart = cartRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 장바구니에 없습니다."));
        cart.setCount(count);
        cartRepository.save(cart);
    }

    //추가 ( 장바구니 전체 삭제)
    public void clearCart(Long userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);

        if (carts.isEmpty()) {
            throw new IllegalArgumentException("장바구니가 이미 비어있습니다.");
        }

        cartRepository.deleteAll(carts);
    }

    public void removeItem(Long userId, Long productId) {
        cartRepository.deleteByUserIdAndProductId(userId, productId);
    }
}
