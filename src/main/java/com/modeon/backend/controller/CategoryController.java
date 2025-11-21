package com.modeon.backend.controller;

import com.modeon.backend.dto.*;
import com.modeon.backend.entity.ProductVariant;
import com.modeon.backend.service.CategoryService;
import com.modeon.backend.service.ProductService;
import com.modeon.backend.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/category")
@RequiredArgsConstructor
public class CategoryController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductVariantService variantService;
    @Value("${ADMIN_URL}")
    private String ADMIN_URL;

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public List<CategoryResponse> getCategoryByParentId(
            @RequestParam(required = false) Long parentId
    ){
        return categoryService.getCategoriesByParentId(parentId);
    }
}
