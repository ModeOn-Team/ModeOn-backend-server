package com.modeon.backend.controller;

import com.modeon.backend.dto.ProductResponse;
import com.modeon.backend.service.FileUploadService;
import com.modeon.backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class FileUploadController {

    private final FileUploadService fileUploadService;
    private final ProductService productService;

    @PostMapping("/product-image")
    public ResponseEntity<ProductResponse> uploadProductImage(
            @RequestParam("productId") Long productId,
            @RequestParam("images")List<MultipartFile> images
            ){
        ProductResponse response = productService.saveProductImages(productId, images);
        return ResponseEntity.ok(response);
    }
}