package com.cinebook.module.showtime.mapper;

import com.cinebook.module.movie.entity.MovieGenre;
import com.cinebook.module.showtime.dto.response.ShowtimeResponse;
import com.cinebook.module.showtime.entity.Showtime;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ShowtimeMapper {

    @Mapping(source = "movie.id", target = "movieId")
    @Mapping(source = "movie.title", target = "movieTitle")
    @Mapping(source = "movie.duration", target = "movieDuration")
    @Mapping(source = "movie.posterUrl", target = "moviePosterUrl")
    @Mapping(source = "room.id", target = "roomId")
    @Mapping(source = "room.name", target = "roomName")
    @Mapping(source = "room.cinema.id", target = "cinemaId")
    @Mapping(source = "room.cinema.name", target = "cinemaName")
    @Mapping(source = "room.cinema.address", target = "cinemaAddress")
    @Mapping(source = "movie.movieGenres", target = "genreNames")
    ShowtimeResponse toResponse(Showtime showtime);

    default String mapGenreName(MovieGenre movieGenre) {
        if (movieGenre == null || movieGenre.getGenre() == null) {
            return null;
        }
        return movieGenre.getGenre().getName();
    }
}
