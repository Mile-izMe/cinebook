package com.cinebook.module.cinema.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.cinema.dto.request.CinemaCreateRequest;
import com.cinebook.module.cinema.dto.response.CinemaResponse;
import com.cinebook.module.cinema.entity.Cinema;
import com.cinebook.module.cinema.mapper.CinemaMapper;
import com.cinebook.module.cinema.repository.CinemaRepository;
import com.cinebook.module.city.entity.City;
import com.cinebook.module.city.repository.CityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CinemaService {

    private final CinemaRepository cinemaRepository;
    private final CityRepository cityRepository;
    private final CinemaMapper cinemaMapper;

    @Transactional(readOnly = true)
    public List<CinemaResponse> list(UUID cityId) {
        List<Cinema> cinemas = (cityId != null)
                ? cinemaRepository.findAllByCityId(cityId)
                : cinemaRepository.findAll();
        return cinemas.stream().map(cinemaMapper::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public CinemaResponse getById(UUID id) {
        return cinemaMapper.toResponse(findOrThrow(id));
    }

    @Transactional
    public CinemaResponse create(CinemaCreateRequest request) {
        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new CinebookException(ErrorCode.CITY_NOT_FOUND));

        Cinema cinema = Cinema.builder()
                .city(city)
                .name(request.name())
                .address(request.address())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .build();

        return cinemaMapper.toResponse(cinemaRepository.save(cinema));
    }

    // ============= HELPER ===============
    public Cinema findOrThrow(UUID id) {
        return cinemaRepository.findById(id)
                .orElseThrow(() -> new CinebookException(ErrorCode.CINEMA_NOT_FOUND));
    }
}
