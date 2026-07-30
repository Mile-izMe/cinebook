package com.cinebook.module.city.mapper;

import com.cinebook.module.city.dto.response.CityResponse;
import com.cinebook.module.city.entity.City;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CityMapper {

    CityResponse toResponse(City city);
}
