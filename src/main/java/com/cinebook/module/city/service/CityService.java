package com.cinebook.module.city.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.city.dto.request.CityCreateRequest;
import com.cinebook.module.city.dto.request.CityUpdateRequest;
import com.cinebook.module.city.dto.response.CityResponse;
import com.cinebook.module.city.entity.City;
import com.cinebook.module.city.mapper.CityMapper;
import com.cinebook.module.city.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CityService {

    private final CityRepository cityRepository;
    private final CityMapper cityMapper;

    @Transactional(readOnly = true)
    public List<CityResponse> list() {
        return cityRepository.findAll().stream().map(cityMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CityResponse getById(UUID id) {
        City city = cityRepository.findById(id)
                .orElseThrow(() -> new CinebookException(ErrorCode.CITY_NOT_FOUND));
        return cityMapper.toResponse(city);
    }

    @Transactional
    public CityResponse create(CityCreateRequest request) {
        City city = City.builder()
                .cityName(request.cityName())
                .build();

        city = cityRepository.save(city);

        return cityMapper.toResponse(city);
    }

    @Transactional
    public CityResponse update(UUID cityId, CityUpdateRequest request) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new CinebookException(ErrorCode.CITY_NOT_FOUND));

        city.setCityName(request.cityName());
        cityRepository.save(city);

        return cityMapper.toResponse(city);
    }

    @Transactional
    public void delete(UUID cityId) {
        City city = cityRepository.findById(cityId)
                .orElseThrow(() -> new CinebookException(ErrorCode.CITY_NOT_FOUND));
        // Soft delete: rely on Auditable.deletedAt, not a real DELETE -
        city.setDeletedAt(Instant.now());
        cityRepository.save(city);
    }
}
