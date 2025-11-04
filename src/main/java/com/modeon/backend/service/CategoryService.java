package com.modeon.backend.service;

import com.modeon.backend.dto.CategoryRequest;
import com.modeon.backend.dto.CategoryResponse;
import com.modeon.backend.entity.Category;
import com.modeon.backend.exception.ResourceNotFoundException;
import com.modeon.backend.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final AuthenticationService authenticationService;

    public CategoryResponse createCategory(CategoryRequest request) {
        authenticationService.checkAdmin();

        Category parent = null;
        int depth = 0;

        if (request.getParentId() != null) {
            parent = categoryRepository.findById(request.getParentId())
                    .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            depth = parent.getDepth() + 1;
        }

        Category category = Category.builder()
                .name(request.getName())
                .parent(parent)
                .depth(depth)
                .build();

        Category saved = categoryRepository.save(category);

        return CategoryResponse.fromEntity(saved);
    }

    public List<CategoryResponse> getAllCategories(){
        authenticationService.checkAdmin();
        List<Category> categories = categoryRepository.findAll();

        return categories.stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }
}