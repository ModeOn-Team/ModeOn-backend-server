package com.modeon.backend.cart.dto;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long productId;
    private int count;
    private String size;
    private String color;
}
