package com.modeon.backend.controller;

import com.modeon.backend.dto.CategoryResponse;
import com.modeon.backend.dto.ProductResponse;
import com.modeon.backend.entity.Color;
import com.modeon.backend.entity.Gender;
import com.modeon.backend.entity.Size;
import com.modeon.backend.service.AuthService;
import com.modeon.backend.service.CategoryService;
import com.modeon.backend.service.ProductService;
import com.modeon.backend.service.WishListService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final WishListService wishListService;

    @GetMapping("/categories")
    public List<CategoryResponse> getAllCategories() {
        return categoryService.getAllCategories();
    }

    @GetMapping("/list")
    public ResponseEntity<Page<ProductResponse>> getAllProduct(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getAllProduct(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/detail/{productId}")
    public ResponseEntity<ProductResponse> getProductDetail(
            @PathVariable Long productId
    ){
        ProductResponse products = productService.getProductDetail(productId);
        return ResponseEntity.ok(products);
    }

    @PostMapping("/{productId}/wishlist")
    public ResponseEntity<?> toggleWishList(@PathVariable Long productId){
        boolean isWishList = wishListService.toggleWishList(productId);

        return ResponseEntity.ok().body(Map.of(
                "isWishList", isWishList
        ));
    }

    @GetMapping("/wishlist")
    public ResponseEntity<Page<ProductResponse>> getMyWishList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.getMyWishList(pageable);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> searchProducts(
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String productSize,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String word,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Gender genderEnum = null;
        if (gender != null && !gender.isEmpty()) {
            try {
                genderEnum = Gender.valueOf(gender.toUpperCase());
            } catch (IllegalArgumentException e) {
                return ResponseEntity.badRequest().build();
            }
        }

        Size sizeEnum = null;
        if (productSize != null && !productSize.isEmpty()) {
            sizeEnum = Size.valueOf(productSize.toUpperCase());
        }

        Color colorEnum = null;
        if (color != null && !color.isEmpty()) {
            colorEnum = Color.valueOf(color.toUpperCase());
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponse> products = productService.searchProduct(
                genderEnum, category, sizeEnum, colorEnum, word, pageable
        );
        return ResponseEntity.ok(products);

    }

}