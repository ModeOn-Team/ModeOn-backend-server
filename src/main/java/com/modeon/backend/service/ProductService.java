package com.modeon.backend.service;

import com.modeon.backend.dto.ProductRequest;
import com.modeon.backend.dto.ProductResponse;
import com.modeon.backend.entity.*;
import com.modeon.backend.exception.ResourceNotFoundException;
import com.modeon.backend.repository.CategoryRepository;
import com.modeon.backend.repository.CommentRepository;
import com.modeon.backend.repository.ProductRepository;
import com.modeon.backend.repository.WishListRepository;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {
    private final AuthenticationService authenticationService;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;
    private final WishListRepository wishListRepository;
    private final CommentRepository commentRepository;

    public ProductResponse createProduct(ProductRequest request){
        authenticationService.checkAdmin();

        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .gender(request.getGender())
                .category(category)
                .build();

        Product saved = productRepository.save(product);

        return ProductResponse.fromEntity(saved);
    }

    public Page<ProductResponse> getAllProduct(Pageable pageable){
        User currentUser = authenticationService.getCurrentUser();
        Page<Product> products = productRepository.findAll(pageable);

        return products.map(product -> {
            ProductResponse response = ProductResponse.fromEntity(product);
            long wishListCount = wishListRepository.countByProductId(product.getId());
            boolean isWishList = wishListRepository.existsByUserAndProduct(currentUser, product);
            Long commentCount = commentRepository.countByProductId(product.getId());


            response.setWishListCount(wishListCount);
            response.setWishList(isWishList);
            response.setCommentCount(commentCount);
            return response;
        });
    }

    public ProductResponse getProductDetail(Long productId){
        User currentUser = authenticationService.getCurrentUser();
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        boolean isWishList = wishListRepository.existsByUserAndProduct(currentUser, product);
        ProductResponse response = ProductResponse.fromEntity(product);
        response.setWishList(isWishList);
        return response;
    }

    public Page<ProductResponse> getMyWishList(
            Pageable pageable
    ) {
        User currentUser = authenticationService.getCurrentUser();
        Page<Product> products = wishListRepository.findProductsByUserId(currentUser.getId(), pageable);

        return products.map(ProductResponse::fromEntity);
    }



    @Transactional
    public ProductResponse saveProductImages(Long productId, List<MultipartFile> images) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        int sortOrder = product.getDetailImages().size();

        for (MultipartFile file : images) {
            String imageUrl;

            if (sortOrder == 0) {
                imageUrl = fileUploadService.uploadThumbnail(file, "product");
            } else {
                imageUrl = fileUploadService.uploadOriginal(file, "product");
            }

            ProductImage image = ProductImage.builder()
                    .imageUrl(imageUrl)
                    .sortOrder(sortOrder++)
                    .product(product)
                    .build();

            product.addImage(image);
        }

        Product response = productRepository.save(product);
        return ProductResponse.fromEntity(response);
    }

    public void deleteProduct(Long productId){
        authenticationService.checkAdmin();

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Post not found"));

        productRepository.delete(product);
    }

    public Page<ProductResponse> searchProduct(
            Gender gender,
            String categoryName,
            Size size,
            Color color,
            String word,
            Pageable pageable
    ) {
        Category category = null;

        User currentUser = authenticationService.getCurrentUser();
        if (categoryName != null && !categoryName.isBlank()) {
            category = categoryRepository.findByName(categoryName)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        }

        Page<Product> products = productRepository.searchProducts(
                gender,
                category != null ? category.getId() : null,
                size,
                color,
                word,
                pageable
        );

        return products.map(product -> {
            ProductResponse response = ProductResponse.fromEntity(product);
            long wishListCount = wishListRepository.countByProductId(product.getId());
            boolean isWishList = wishListRepository.existsByUserAndProduct(currentUser, product);
            Long commentCount = commentRepository.countByProductId(product.getId());

            response.setWishListCount(wishListCount);
            response.setWishList(isWishList);
            response.setCommentCount(commentCount);
            return response;
        });
    }
}
