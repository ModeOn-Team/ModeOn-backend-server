package com.modeon.backend.cart.dto;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long productId;  // 상품 ID (참고용, variant에서 얻을 수 있음)
    private Long variantId;  // ProductVariant ID (필수)
    private int count;
}
