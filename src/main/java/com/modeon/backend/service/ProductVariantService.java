package com.modeon.backend.service;

import com.modeon.backend.dto.NaverProductVariantRequest;
import com.modeon.backend.dto.ProductResponse;
import com.modeon.backend.dto.ProductVariantRequest;
import com.modeon.backend.dto.ProductVariantResponse;
import com.modeon.backend.entity.Product;
import com.modeon.backend.entity.ProductVariant;
import com.modeon.backend.repository.ProductRepository;
import com.modeon.backend.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductVariantService {

    private final ProductVariantRepository variantRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProductVariant createVariant(Long productId, ProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        Optional<ProductVariant> existingVariantOpt =
                variantRepository.findByProductIdAndColorAndSize(productId, request.getColor(), request.getSize());

        ProductVariant variant;

        if (existingVariantOpt.isPresent()) {
            variant = existingVariantOpt.get();
            int newStock = variant.getStock() + request.getStock();
            variant.setStock(newStock);
        } else {
            variant = ProductVariant.builder()
                    .product(product)
                    .color(request.getColor())
                    .size(request.getSize())
                    .stock(request.getStock())
                    .build();
        }

        return variantRepository.save(variant);
    }

    @Transactional
    public ProductVariant createVariantNaver(Long productId, NaverProductVariantRequest request) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));


        String colorId = null;
        String sizeId = null;
        String color = null;
        String size = null;

        // 옵션 리스트에서 color, size 추출
        for (NaverProductVariantRequest.OptionValue opt : request.getOptions()) {
            if ("색상".equals(opt.getOptionName())) {
                color = opt.getValueName();
                colorId = opt.getOptionId();
            } else if ("사이즈".equals(opt.getOptionName())) {
                size = opt.getValueName();
                sizeId = opt.getOptionId();
            }
        }
        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .naverColorId(colorId)
                .color(color)
                .naverSizeId(sizeId)
                .size(size)
                .stock(request.getStock())
                .build();

        return variantRepository.save(variant);
    }

    @Transactional
    public ProductVariant updateVariant(Long variantId, ProductVariantRequest request) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variant not found"));

        int updatedStock = variant.getStock() + request.getStock();
        if (updatedStock < 0) {
            throw new IllegalArgumentException("Stock cannot be negative");
        }

        variant.setStock(updatedStock);

        return variantRepository.save(variant);
    }
}
