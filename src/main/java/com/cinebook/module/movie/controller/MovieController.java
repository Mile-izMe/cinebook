package com.cinebook.module.movie.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.common.util.CursorPageResponse;
import com.cinebook.module.movie.dto.request.MovieCreateRequest;
import com.cinebook.module.movie.dto.request.MovieUpdateRequest;
import com.cinebook.module.movie.dto.response.MovieDetailResponse;
import com.cinebook.module.movie.dto.response.MovieSummaryResponse;
import com.cinebook.module.movie.service.MovieService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    // ---- Admin-only writes, enforced in SecurityConfig ----
    @PostMapping
    public ResponseEntity<ApiSuccessResponse<MovieSummaryResponse>> create(
            @Valid @RequestBody MovieCreateRequest request) {
        MovieSummaryResponse movieResponse = movieService.create(request);

        ApiSuccessResponse<MovieSummaryResponse> response = ApiSuccessResponse.<MovieSummaryResponse>builder()
                .message("Movie created successful")
                .data(movieResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<MovieSummaryResponse>> update(@PathVariable UUID id,
                                                                           @Valid @RequestBody MovieUpdateRequest request) {
        MovieSummaryResponse movieResponse = movieService.update(id, request);

        ApiSuccessResponse<MovieSummaryResponse> response = ApiSuccessResponse.<MovieSummaryResponse>builder()
                .message("Movie updated successful")
                .data(movieResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- public read + search ----
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<MovieSummaryResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID genre,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        int safeLimit = Math.min(limit, 50);
        CursorPageResponse<MovieSummaryResponse> page = movieService.search(keyword, genre, cursor, safeLimit);

        return ResponseEntity.ok(ApiSuccessResponse.ofCursorPage(page, safeLimit, "Get list movies successful"));
    }


    // ---- Movie detail, the most important endpoint ----
    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<MovieDetailResponse>> getDetail(@PathVariable UUID id) {
        MovieDetailResponse detail = movieService.getDetail(id);

        ApiSuccessResponse<MovieDetailResponse> response = ApiSuccessResponse.<MovieDetailResponse>builder()
                .message("Get detail successful")
                .data(detail)
                .build();

        return ResponseEntity.ok(response);
    }
}
