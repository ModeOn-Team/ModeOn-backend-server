package com.modeon.backend.controller;

import com.modeon.backend.dto.CommentRequest;
import com.modeon.backend.dto.CommentResponse;
import com.modeon.backend.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/product/{productId}")
    public ResponseEntity<CommentResponse> createComment(@PathVariable Long productId, @Valid @RequestBody CommentRequest request){
        CommentResponse response = commentService.createComment(productId, request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<Page<CommentResponse>> getComments(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size){
        Pageable pageable = PageRequest.of(page, size);
        Page<CommentResponse> comments = commentService.getComments(productId, pageable);
        return ResponseEntity.ok(comments);
    }
}