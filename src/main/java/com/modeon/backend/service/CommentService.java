package com.modeon.backend.service;

import com.modeon.backend.dto.CommentRequest;
import com.modeon.backend.dto.CommentResponse;
import com.modeon.backend.entity.Comment;
import com.modeon.backend.entity.Product;
import com.modeon.backend.entity.User;
import com.modeon.backend.exception.ResourceNotFoundException;
import com.modeon.backend.repository.CommentRepository;
import com.modeon.backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final AuthenticationService authenticationService;
    private final ProductRepository productRepository;

    public CommentResponse createComment(Long productId, CommentRequest request){
        User user = authenticationService.getCurrentUser();
        Product product = productRepository.findById(productId).orElseThrow(()->new ResourceNotFoundException("Product not found"));

        Comment comment = Comment.builder()
                .content(request.getContent())
                .product(product)
                .user(user)
                .build();

        comment = commentRepository.save(comment);
        return CommentResponse.fromEntity(comment);
    }

    @Transactional(readOnly = true)
    public Page<CommentResponse> getComments(Long productId, Pageable pageable) {
        authenticationService.getCurrentUser();

        productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Page<Comment> comments = commentRepository.findByProductId(productId, pageable);
        return comments.map(CommentResponse::fromEntity);
    }
}