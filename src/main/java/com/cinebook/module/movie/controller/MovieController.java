package com.cinebook.module.movie.controller;

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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/movies")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;
//    private final FileStorageService fileStorageService;

    // ---- Admin-only writes, enforced in SecurityConfig ----

    @PostMapping
    public ResponseEntity<MovieSummaryResponse> create(@Valid @RequestBody MovieCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(movieService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MovieSummaryResponse> update(@PathVariable UUID id,
                                                       @Valid @RequestBody MovieUpdateRequest request) {
        return ResponseEntity.ok(movieService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        movieService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- public read + search ----

    @GetMapping
    public ResponseEntity<CursorPageResponse<MovieSummaryResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UUID genre,
            @RequestParam(required = false) String cursor,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(movieService.search(keyword, genre, cursor, Math.min(limit, 50)));
    }

    // ---- Poster upload, admin-only ----

//    @PostMapping(value = "/{id}/poster", consumes = "multipart/form-data")
//    public ResponseEntity<Map<String, String>> uploadPoster(@PathVariable UUID id,
//                                                            @RequestParam("file") MultipartFile file) {
//        String posterUrl = fileStorageService.uploadPoster(id, file);
//        movieService.updatePosterUrl(id, posterUrl);
//        return ResponseEntity.ok(Map.of("posterUrl", posterUrl));
//    }

    // ---- Movie detail, the most important endpoint ----

    @GetMapping("/{id}")
    public ResponseEntity<MovieDetailResponse> getDetail(@PathVariable UUID id) {
        return ResponseEntity.ok(movieService.getDetail(id));
    }
}
