package com.modeon.backend.dto;


import lombok.Data;

@Data
public class ProductVariantRequest {
    private String size;
    private String color;
    private Integer stock;
}