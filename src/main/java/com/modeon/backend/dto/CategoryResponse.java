package com.modeon.backend.dto;

import com.modeon.backend.entity.Category;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryResponse {
    private Long id;
    private String name;
    private Integer depth;
    private Long parentId;

    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .depth(category.getDepth())
                .parentId(category.getParent() != null ? category.getParent().getId() : null)
                .build();
    }
}