package com.modeon.backend.cart.service;

import com.modeon.backend.cart.dto.CartItemRequest;
import com.modeon.backend.cart.dto.CartItemResponse;
import com.modeon.backend.cart.entity.Cart;
import com.modeon.backend.cart.repository.CartRepository;
import com.modeon.backend.entity.User;
import com.modeon.backend.entity.Product;
import com.modeon.backend.repository.ProductRepository;
import com.modeon.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Transactional
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

        // 기존 장바구니에 같은 상품, 사이즈, 색상 조합이 있는지 확인
        Cart existingCart = cartRepository.findByUserIdAndProductIdAndSizeAndColor(userId, request.getProductId(), request.getSize(), request.getColor())
                .orElse(null);

        if (existingCart == null) {
            // 새 상품 추가
            Cart newCart = Cart.builder()
                    .user(user)
                    .product(product)
                    .count(request.getCount())
                    .size(request.getSize())
                    .color(request.getColor())
                    .build();
            cartRepository.save(newCart);
        } else {
            // 기존 상품 수량 누적
            existingCart.setCount(existingCart.getCount() + request.getCount());
            cartRepository.save(existingCart);
        }
    }


    public List<CartItemResponse> getCart(Long userId) {
        List<Cart> carts = cartRepository.findByUserId(userId);

        return carts.stream()
                .map(cart -> CartItemResponse.builder()
                        .id(cart.getId())
                        .count(cart.getCount())
                        .productId(cart.getProduct().getId())
                        .productName(cart.getProduct().getName())
                        .productPrice(cart.getProduct().getPrice())
                        .productImage(
                                cart.getProduct().getDetailImages().isEmpty() || cart.getProduct().getDetailImages() == null
                                        ? "default-image-url"  // 기본 이미지 URL로 대체
                                        : cart.getProduct().getDetailImages().get(0).getImageUrl()
                        )
                        .size(cart.getSize())
                        .color(cart.getColor())
                        .build()
                ).toList();
    }


    public void updateCount(Long userId, Long cartId, int count) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("상품이 장바구니에 없습니다."));

        if (cart.getUser().getId() != userId) {
            throw new IllegalArgumentException("본인의 장바구니만 수정할 수 있습니다.");
        }

        cart.setCount(count);
        cartRepository.save(cart);
    }




    public void removeItemByCartId(Long userId, Long cartId) {
         int deleted = cartRepository.deleteByIdAndUserId(cartId, userId);
        if (deleted == 0) {
            throw new IllegalArgumentException("삭제할 항목이 없습니다.");
        }
    }

}
