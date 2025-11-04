package com.modeon.backend.controller;

import com.modeon.backend.dto.*;
import com.modeon.backend.entity.ProductVariant;
import com.modeon.backend.service.CategoryService;
import com.modeon.backend.service.ProductService;
import com.modeon.backend.service.ProductVariantService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/${ADMIN_URL}")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ProductVariantService variantService;

    @GetMapping("/Health")
    public String HealthCheck(){
        return "잘된다";
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(@RequestBody CategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/categories")
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
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

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId){
        productService.deleteProduct(productId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/product-images")
    public ResponseEntity<ProductResponse> uploadProductImage(
            @RequestParam("productId") Long productId,
            @RequestParam("images") List<MultipartFile> images
    ){
        ProductResponse response = productService.saveProductImages(productId, images);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/products/{productId}/variants")
    public ResponseEntity<ProductVariantResponse> createVariant(
            @PathVariable Long productId,
            @RequestBody ProductVariantRequest request) {

        ProductVariant variant = variantService.createVariant(productId, request);
        return ResponseEntity.ok(ProductVariantResponse.fromEntity(variant));
    }

    @PutMapping("/products/variants/{variantId}")
    public ResponseEntity<ProductVariantResponse> updateVariant(
            @PathVariable Long variantId,
            @RequestBody ProductVariantRequest request) {

        ProductVariant updated = variantService.updateVariant(variantId, request);
        return ResponseEntity.ok(ProductVariantResponse.fromEntity(updated));
    }
}
