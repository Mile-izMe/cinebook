package com.cinebook.module.review.controller;

import com.cinebook.common.util.CursorPageResponse;
import com.cinebook.module.review.dto.request.ReviewRequest;
import com.cinebook.module.review.dto.response.ReviewResponse;
import com.cinebook.module.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/movies/{movieId}/reviews")
@RequiredArgsConstructor
public class ReviewController {


    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@PathVariable UUID movieId,
                                                 @Valid @RequestBody ReviewRequest request,
                                                 Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(movieId, userId, request));
    }

    @GetMapping
    public ResponseEntity<CursorPageResponse<ReviewResponse>> list(
            @PathVariable UUID movieId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(reviewService.list(movieId, cursor, Math.min(limit, 50)));
    }
}
