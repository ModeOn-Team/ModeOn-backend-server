package com.modeon.backend.dto;

import com.modeon.backend.entity.Category;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDto {
    private Long id;
    private String name;
    private String largeCategory;
    private String middleCategory;
    private String smallCategory;

    public static CategoryDto fromEntity(Category category){
        if(category == null) return null;

        Category middle = category.getParent();
        Category large = middle != null ? middle.getParent() : null;

        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .smallCategory(category.getName())
                .middleCategory(middle != null ? middle.getName() : null)
                .largeCategory(large != null ? large.getName() : null)
                .build();
    }
}