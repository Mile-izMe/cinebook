package com.cinebook.module.showtime.dto.response;

import com.cinebook.module.cinema.entity.Cinema;
import com.cinebook.module.movie.entity.Movie;
import com.cinebook.module.room.entity.Room;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SeatMapResponse(
        UUID showtimeId,
        MovieSummary movie,
        CinemaSummary cinema,
        RoomSummary room,
        LocalDateTime startTime,
        String format,
        List<SeatMapRow> rows
) {

    public record MovieSummary(UUID id, String title, String posterUrl, Integer duration, List<String> genreNames) {
        public static MovieSummary from(Movie movie) {
            return new MovieSummary(movie.getId(), movie.getTitle(), movie.getPosterUrl(), movie.getDuration(),
                    movie.getMovieGenres().stream().map(movieGenre -> movieGenre.getGenre().getName()).toList());
        }
    }

    public record CinemaSummary(UUID id, String name, String address) {
        public static CinemaSummary from(Cinema cinema) {
            return new CinemaSummary(cinema.getId(), cinema.getName(), cinema.getAddress());
        }
    }

    public record RoomSummary(UUID id, String name) {
        public static RoomSummary from(Room room) {
            return new RoomSummary(room.getId(), room.getName());
        }
    }

    public record SeatMapRow(
            String row,
            List<SeatMapSeat> seats
    ) {
    }

    public record SeatMapSeat(
            UUID seatId,
            String label,
            String type,
            Integer price,
            String status
    ) {
    }
}
