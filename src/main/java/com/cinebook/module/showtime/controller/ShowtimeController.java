package com.cinebook.module.showtime.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.module.showtime.dto.request.ShowtimeCreateRequest;
import com.cinebook.module.showtime.dto.response.ShowtimeResponse;
import com.cinebook.module.showtime.service.ShowtimeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ShowtimeController {

    private final ShowtimeService showtimeService;

    @PostMapping("/api/showtimes")
    public ResponseEntity<ApiSuccessResponse<ShowtimeResponse>> create(
            @Valid @RequestBody ShowtimeCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.<ShowtimeResponse>builder()
                .message("Create showtime successful!")
                .data(showtimeService.create(request))
                .build());
    }

    @GetMapping("/api/movies/{movieId}/showtimes")
    public ResponseEntity<ApiSuccessResponse<List<ShowtimeResponse>>> listByMovie(
            @PathVariable UUID movieId,
            @RequestParam(required = false) UUID cinemaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(ApiSuccessResponse.<List<ShowtimeResponse>>builder()
                .message("Get list showtime successful!")
                .data(showtimeService.listByMovie(movieId, cinemaId, date))
                .build());
    }

    @GetMapping("/api/showtimes/{id}")
    public ResponseEntity<ApiSuccessResponse<ShowtimeResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiSuccessResponse.<ShowtimeResponse>builder()
                .message("Get detail showtime successful!")
                .data(showtimeService.getById(id))
                .build());
    }
}
