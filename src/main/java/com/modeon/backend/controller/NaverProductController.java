package com.modeon.backend.controller;

import com.modeon.backend.dto.*;
import com.modeon.backend.entity.Category;
import com.modeon.backend.entity.ProductVariant;
import com.modeon.backend.repository.CategoryRepository;
import com.modeon.backend.repository.ProductVariantRepository;
import com.modeon.backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class NaverProductController {
    private final ProductService productService;
    private final NaverProductService naverProductService;
    private final NaverProductImageService naverProductImageService;
    private final CategoryRepository categoryRepository;
    private final NaverProductVariantService naverProductVariantService;
    private final ProductVariantService productVariantService;
    private final ProductVariantRepository productVariantRepository;

    @PostMapping("/upload-naver/{productId}")
    public void ProductUploadToNaver(
            @PathVariable Long productId,
            @RequestBody NaverProductImageDto imageDto
    ){
        ProductResponse products = productService.getProductDetail(productId);
        List<ProductVariant> variant = productVariantRepository.findByProductId(productId);
        naverProductService.uploadProduct(products, imageDto, variant);
    }

    @PostMapping(value = "/image/upload-naver/", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<NaverProductImageDto> uploadImages(
            @RequestParam("images") List<MultipartFile> imageFiles
    ) throws IOException {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        if (imageFiles.size() > 10) {
            return ResponseEntity.badRequest().body(null);
        }

        NaverProductImageDto response = naverProductImageService.uploadImage(imageFiles);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/variant/load-naver/{categoryId}")
    public String VariantLoadToNaver (
            @PathVariable Long categoryId
    ){
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        return naverProductVariantService.loadOptionGuides(category.getNaverCategoryId());
    }

    @PostMapping("/variant/{productId}/naver")
    public ResponseEntity<ProductVariantResponse> createVariant(
            @PathVariable Long productId,
            @RequestBody NaverProductVariantRequest request) {

        ProductVariant variant = productVariantService.createVariantNaver(productId, request);
        return ResponseEntity.ok(ProductVariantResponse.fromEntity(variant));
    }
}
