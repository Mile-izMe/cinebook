package com.cinebook.module.seat.mapper;

import com.cinebook.module.seat.dto.response.SeatResponse;
import com.cinebook.module.seat.entity.Seat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    @Mapping(source = "room.id", target = "roomId")
    @Mapping(target = "label", expression = "java(seat.label())")
    SeatResponse toResponse(Seat seat);
}
