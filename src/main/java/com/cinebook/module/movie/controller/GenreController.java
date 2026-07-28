package com.cinebook.module.movie.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.module.movie.dto.response.GenreResponse;
import com.cinebook.module.movie.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/genres")
@RequiredArgsConstructor
public class GenreController {

    private final GenreRepository genreRepository;

    // Read-only, public - genres are reference data seeded via Flyway.
    // No admin CRUD endpoint for now; add one later if the list needs to grow at runtime.
    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<GenreResponse>>> list() {
        List<GenreResponse> genres = genreRepository.findAll().stream()
                .map(g -> new GenreResponse(g.getId(), g.getName()))
                .toList();

        ApiSuccessResponse<List<GenreResponse>> response = ApiSuccessResponse.<List<GenreResponse>>builder()
                .message("Get genres successful")
                .data(genres)
                .build();

        return ResponseEntity.ok(response);
    }
}
