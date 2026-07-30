package com.cinebook.module.room.mapper;

import com.cinebook.module.room.dto.response.RoomResponse;
import com.cinebook.module.room.entity.Room;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RoomMapper {

    @Mapping(source = "cinema.id", target = "cinemaId")
    RoomResponse toResponse(Room room);
}
