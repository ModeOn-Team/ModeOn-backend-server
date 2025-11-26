package com.modeon.backend.dto;

import com.modeon.backend.entity.ProductVariant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductVariantResponse {
    private Long id;
    private String size;
    private String color;
    private Integer stock;

    public static ProductVariantResponse fromEntity(ProductVariant variant){
        return ProductVariantResponse.builder()
                .id(variant.getId())
                .size(variant.getSize())
                .color(variant.getColor())
                .stock(variant.getStock())
                .build();
    }
}
