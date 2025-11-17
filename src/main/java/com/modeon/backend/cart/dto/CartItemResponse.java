package com.modeon.backend.cart.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CartItemResponse {
    private Long id;
    private int count;
    private Long productId;
    private String productName;
    private int productPrice;
    private String productImage;
}
