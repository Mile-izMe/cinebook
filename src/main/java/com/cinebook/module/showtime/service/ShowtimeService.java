package com.cinebook.module.showtime.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.movie.entity.Movie;
import com.cinebook.module.movie.service.MovieService;
import com.cinebook.module.room.entity.Room;
import com.cinebook.module.room.service.RoomService;
import com.cinebook.module.showtime.dto.request.ShowtimeCreateRequest;
import com.cinebook.module.showtime.dto.response.ShowtimeResponse;
import com.cinebook.module.showtime.entity.Showtime;
import com.cinebook.module.showtime.mapper.ShowtimeMapper;
import com.cinebook.module.showtime.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieService movieService;
    private final RoomService roomService;
    private final ShowtimeMapper showtimeMapper;

    @Transactional
    public ShowtimeResponse create(ShowtimeCreateRequest request) {
        Movie movie = movieService.findOrThrow(request.movieId());

        Room room = roomService.findOrThrow(request.roomId());

        var endTime = request.startTime().plusMinutes(movie.getDuration());

        // Rely on DB UNIQUE(room_id, start_time) as the real race guard;
        // this pre-check is just a friendlier error for the common case
        boolean overlap = showtimeRepository.existsByRoomIdAndStartTimeBetween(
                request.roomId(), request.startTime().minusMinutes(movie.getDuration()), endTime);
        if (overlap) {
            throw new CinebookException(ErrorCode.SHOWTIME_OVERLAP);
        }

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .startTime(request.startTime())
                .endTime(endTime)
                .format(request.format())
                .basePrice(request.basePrice())
                .build();

        return showtimeMapper.toResponse(showtimeRepository.save(showtime));
    }

    @Transactional(readOnly = true)
    public List<ShowtimeResponse> listByMovie(UUID movieId, UUID cityId, UUID cinemaId, String format, LocalDate date) {
        return showtimeRepository.findByMovieAndFilters(movieId, cityId, cinemaId, format, date).stream()
                .map(showtimeMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public ShowtimeResponse getById(UUID id) {
        return showtimeMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public Showtime findOrThrow(UUID id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new CinebookException(ErrorCode.SHOWTIME_NOT_FOUND));
    }
}
