package com.modeon.backend.dto;

import com.modeon.backend.entity.*;
import lombok.Data;

@Data
public class ProductRequest {
    private String name;
    private Integer price;
    private Gender gender;
    private Long categoryId;
}
