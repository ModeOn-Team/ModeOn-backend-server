package com.modeon.backend.dto;

import com.modeon.backend.entity.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProductResponse {
    private Long id;
    private String name;
    private Integer price;
    private Integer stock;
    private Size size;
    private Color color;
    private Gender gender;
    private CategoryDto category;
//    private List<String> detailImages = new ArrayList<>();

    public static ProductResponse fromEntity(Product product){
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .stock(product.getStock())
                .size(product.getSize())
                .color(product.getColor())
                .gender(product.getGender())
                .category(CategoryDto.fromEntity(product.getCategory()))
//                .detailImages(product.getDetailImages().stream().map(ProductImage::getImageUrl).toList())
                .build();
    }
}
