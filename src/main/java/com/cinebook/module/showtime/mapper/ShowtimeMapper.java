package com.cinebook.module.showtime.mapper;

import com.cinebook.module.showtime.dto.response.ShowtimeResponse;
import com.cinebook.module.showtime.entity.Showtime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShowtimeMapper {

    @Mapping(source = "movie.id", target = "movieId")
    @Mapping(source = "movie.title", target = "movieTitle")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.name", target = "roomName")
    @Mapping(source = "room.cinema.id", target = "cinemaId")
    @Mapping(source = "room.cinema.name", target = "cinemaName")
    ShowtimeResponse toResponse(Showtime showtime);
}
