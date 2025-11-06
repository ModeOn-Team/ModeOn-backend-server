package com.modeon.backend.cart.controller;

import com.modeon.backend.cart.dto.CartItemRequest;
import com.modeon.backend.cart.entity.Cart;
import com.modeon.backend.cart.service.CartService;
import com.modeon.backend.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    //추가   /api/cart/{userId}
    @PostMapping
    public ResponseEntity<String> addItem(
            @AuthenticationPrincipal User user,
            @RequestBody CartItemRequest request
    ) {
        cartService.addItem(user.getId(), request);
        return ResponseEntity.ok("상품이 장바구니에 추가되었습니다.");
    }


    //조회   /api/cart/{userId}/
    @GetMapping
    public ResponseEntity<List<Cart>> getCartItems(@AuthenticationPrincipal User user) {
        List<Cart> items = cartService.getCartItems(user.getId());
        return ResponseEntity.ok(items);
    }

    //수정   /api/cart/{userId}/{productId}
    @PatchMapping("/{productId}")
    public ResponseEntity<String> updateCount(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId,
            @RequestParam int count
    ) {
        cartService.updateCount(user.getId(), productId, count);
        return ResponseEntity.ok("상품 수량이 수정되었습니다. ");
    }

    //삭제
    // (상품 선택 삭제)/{productId}
    @DeleteMapping("/{productId}")
    public ResponseEntity<String> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long productId
    ) {
        cartService.removeItem(user.getId(), productId);
        return ResponseEntity.ok("상품이 장바구니에서 삭제되었습니다.");
    }

    //(상품 전체 삭제)
    @DeleteMapping
    public ResponseEntity<String> clearCart(@AuthenticationPrincipal User user){
        cartService.clearCart(user.getId());
        return  ResponseEntity.ok("장바구니가 비워졌습니다. ");
    }

    //장바구니 선택된 아이템 결제  /api /cart/payment/cart/{userId}
    //@PostMapping("/payment")
    //public ResponseEntity<String> paySelectedItems(@AuthenticationPrincipal User user)
}
