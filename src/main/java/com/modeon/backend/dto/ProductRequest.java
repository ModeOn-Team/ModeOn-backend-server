package com.modeon.backend.dto;

import com.modeon.backend.entity.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ProductRequest {
    private String name;
    private Integer price;
    private Integer stock;
    private Size size;
    private Color color;
    private Gender gender;
    private Long categoryId;
//    private List<ProductImage> detailImages = new ArrayList<>();
}
