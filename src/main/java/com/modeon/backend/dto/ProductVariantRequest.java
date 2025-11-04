package com.modeon.backend.dto;


import com.modeon.backend.entity.Color;
import com.modeon.backend.entity.Size;
import lombok.Data;

@Data
public class ProductVariantRequest {
    private Size size;
    private Color color;
    private Integer stock;
}