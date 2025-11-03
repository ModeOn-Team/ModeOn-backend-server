package com.modeon.backend.service;

import com.modeon.backend.dto.ProductRequest;
import com.modeon.backend.dto.ProductResponse;
import com.modeon.backend.entity.Category;
import com.modeon.backend.entity.Product;
import com.modeon.backend.exception.ResourceNotFoundException;
import com.modeon.backend.repository.CategoryRepository;
import com.modeon.backend.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class ProductService {
    private final AuthenticationService authenticationService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    public ProductResponse createProduct(ProductRequest request){
        authenticationService.checkAdmin();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .size(request.getSize())
                .color(request.getColor())
                .gender(request.getGender())
                .category(category)
                .build();

        Product saved = productRepository.save(product);

        return ProductResponse.fromEntity(saved);
    }

    public Page<ProductResponse> getAllProduct(Pageable pageable){
        authenticationService.checkAdmin();
        Page<Product> products = productRepository.findAll(pageable);

        return products.map(ProductResponse::fromEntity);
    }
}
