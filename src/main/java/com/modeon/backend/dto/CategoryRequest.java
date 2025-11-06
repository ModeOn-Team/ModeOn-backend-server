package com.modeon.backend.dto;

import lombok.Data;

@Data
public class CategoryRequest {
    private String name;
    private Long parentId;
}