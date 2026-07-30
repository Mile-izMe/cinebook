package com.cinebook.module.cinema.controller;


import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.module.cinema.dto.request.CinemaCreateRequest;
import com.cinebook.module.cinema.dto.response.CinemaResponse;
import com.cinebook.module.cinema.service.CinemaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class CinemaController {

    private final CinemaService cinemaService;

    @GetMapping("/api/cinemas")
    public ResponseEntity<ApiSuccessResponse<List<CinemaResponse>>> list(
            @RequestParam(required = false) UUID cityId) {
        return ResponseEntity.ok(ApiSuccessResponse.<List<CinemaResponse>>builder()
                .message("Get list cinemas successful!")
                .data(cinemaService.list(cityId))
                .build());
    }

    @GetMapping("/api/cinemas/{id}")
    public ResponseEntity<ApiSuccessResponse<CinemaResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiSuccessResponse.<CinemaResponse>builder()
                .message("Get detail cinema successful!")
                .data(cinemaService.getById(id))
                .build());
    }

    @GetMapping("/api/cities/{cityId}/cinemas")
    public ResponseEntity<ApiSuccessResponse<List<CinemaResponse>>> listByCity(@PathVariable UUID cityId) {
        return ResponseEntity.ok(ApiSuccessResponse.<List<CinemaResponse>>builder()
                .message("Get list cinemas by city successful!")
                .data(cinemaService.list(cityId))
                .build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/api/cinemas")
    public ResponseEntity<ApiSuccessResponse<CinemaResponse>> create(@Valid @RequestBody CinemaCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.<CinemaResponse>builder()
                .message("Create cinema successful!")
                .data(cinemaService.create(request))
                .build());
    }
}
