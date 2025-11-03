package com.modeon.backend.controller;

import com.modeon.backend.dto.CategoryRequest;
import com.modeon.backend.dto.CategoryResponse;
import com.modeon.backend.dto.ProductRequest;
import com.modeon.backend.dto.ProductResponse;
import com.modeon.backend.service.CategoryService;
import com.modeon.backend.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/${ADMIN_URL}")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/Health")
    public String HealthCheck(){
        return "잘된다";
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/product")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request){
        ProductResponse response = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/product")
    public ResponseEntity<Page<ProductResponse>> getAllProduct(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getAllProduct(pageable);
        return ResponseEntity.ok(products);
    }
}
