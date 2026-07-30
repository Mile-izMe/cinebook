package com.cinebook.module.city.controller;

import com.cinebook.common.response.ApiSuccessResponse;
import com.cinebook.module.city.dto.request.CityCreateRequest;
import com.cinebook.module.city.dto.request.CityUpdateRequest;
import com.cinebook.module.city.dto.response.CityResponse;
import com.cinebook.module.city.service.CityService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @GetMapping
    public ResponseEntity<ApiSuccessResponse<List<CityResponse>>> list() {
        return ResponseEntity.ok(ApiSuccessResponse.<List<CityResponse>>builder()
                .message("Get list cities successful!")
                .data(cityService.list())
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<CityResponse>> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiSuccessResponse.<CityResponse>builder()
                .message("Get city detail successful!")
                .data(cityService.getById(id))
                .build());
    }

    @PostMapping
    public ResponseEntity<ApiSuccessResponse<CityResponse>> create(@Valid @RequestBody CityCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiSuccessResponse.<CityResponse>builder()
                .message("Create city successful!")
                .data(cityService.create(request))
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiSuccessResponse<CityResponse>> update(@PathVariable UUID id,
                                                                   @Valid @RequestBody CityUpdateRequest request) {
        return ResponseEntity.ok(ApiSuccessResponse.<CityResponse>builder()
                .message("Update city successful!")
                .data(cityService.update(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        cityService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
