package com.cinebook.module.review.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.common.security.CustomerUserDetails;
import com.cinebook.common.util.CursorPageResponse;
import com.cinebook.module.review.dto.request.ReviewRequest;
import com.cinebook.module.review.dto.response.ReviewResponse;
import com.cinebook.module.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies/{movieId}/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ReviewResponse> create(@PathVariable UUID movieId,
                                                 @Valid @RequestBody ReviewRequest request,
                                                 @AuthenticationPrincipal CustomerUserDetails userDetails) {
        UUID userId = userDetails.getUserId();
        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.create(movieId, userId, request));
    }

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<ReviewResponse>>> list(
            @PathVariable UUID movieId,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        int safeLimit = Math.min(limit, 50);
        CursorPageResponse<ReviewResponse> page = reviewService.list(movieId, cursor, safeLimit);

        return ResponseEntity.ok(ApiSuccessResponse.ofCursorPage(page, safeLimit, "Get reviews successful"));
    }
}
