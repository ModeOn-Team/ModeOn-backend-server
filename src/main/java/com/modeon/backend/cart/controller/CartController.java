package com.modeon.backend.cart.controller;

import com.modeon.backend.cart.dto.CartItemRequest;
import com.modeon.backend.cart.dto.CartItemResponse;
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

    @PostMapping
    public ResponseEntity<String> addItem(
            @AuthenticationPrincipal User user,
            @RequestBody CartItemRequest request
    ) {
        cartService.addItem(user.getId(), request);
        return ResponseEntity.ok("상품이 장바구니에 추가되었습니다.");
    }



    @GetMapping
    public ResponseEntity<List<CartItemResponse>> getCartItems(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(cartService.getCart(user.getId()));
    }



    //수정   /api/cart/{userId}/{productId}
    @PatchMapping("/item/{cartId}")
    public ResponseEntity<String> updateCount(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartId,
            @RequestParam int count
    ) {
        cartService.updateCount(user.getId(), cartId, count);
        return ResponseEntity.ok("상품 수량이 수정되었습니다.");
    }


    //삭제
    @DeleteMapping("/item/{cartId}")
    public ResponseEntity<String> removeItem(
            @AuthenticationPrincipal User user,
            @PathVariable Long cartId
    ){
        cartService.removeItemByCartId(user.getId(), cartId);
        return ResponseEntity.ok("상품이 장바구니에서 삭제되었습니다.");
    }

}
