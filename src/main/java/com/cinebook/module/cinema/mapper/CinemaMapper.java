package com.cinebook.module.cinema.mapper;

import com.cinebook.module.cinema.dto.response.CinemaResponse;
import com.cinebook.module.cinema.entity.Cinema;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CinemaMapper {

    @Mapping(source = "city.id", target = "cityId")
    CinemaResponse toResponse(Cinema cinema);
}
