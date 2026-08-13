package com.cinebook.module.showtime.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.booking.repository.BookingSeatRepository;
import com.cinebook.module.cinema.entity.Cinema;
import com.cinebook.module.cinema.service.CinemaService;
import com.cinebook.module.movie.entity.Movie;
import com.cinebook.module.movie.service.MovieService;
import com.cinebook.module.room.entity.Room;
import com.cinebook.module.room.service.RoomService;
import com.cinebook.module.seat.entity.Seat;
import com.cinebook.module.seat.repository.SeatRepository;
import com.cinebook.module.showtime.dto.request.ShowtimeCreateRequest;
import com.cinebook.module.showtime.dto.response.SeatMapResponse;
import com.cinebook.module.showtime.dto.response.ShowtimeResponse;
import com.cinebook.module.showtime.entity.Showtime;
import com.cinebook.module.showtime.mapper.ShowtimeMapper;
import com.cinebook.module.showtime.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;

    private final MovieService movieService;
    private final RoomService roomService;
    private final CinemaService cinemaService;
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
    public SeatMapResponse getSeatMap(UUID showtimeId) {
        Showtime showtime = findOrThrow(showtimeId);
        UUID movieId = showtime.getMovie().getId();
        UUID roomId = showtime.getRoom().getId();
        Movie movie = movieService.findOrThrow(movieId);
        Room room = roomService.findOrThrow(roomId);
        Cinema cinema = cinemaService.findOrThrow(room.getCinema().getId());

        List<Seat> seats = seatRepository.findAllByRoomIdOrderByRowAscNumberAsc(roomId);

        Set<UUID> soldSeatIds = bookingSeatRepository.findSoldSeatIdsByShowtimeId(showtimeId);

        // Group by row, keep A -> B -> C...
        Map<String, List<Seat>> grouped = seats.stream()
                .collect(Collectors.groupingBy(Seat::getRow, java.util.LinkedHashMap::new, Collectors.toList()));

        List<SeatMapResponse.SeatMapRow> rows = grouped.entrySet().stream()
                .map(entry -> new SeatMapResponse.SeatMapRow(
                        entry.getKey(),
                        entry.getValue().stream()
                                .sorted(Comparator.comparing(Seat::getNumber))
                                .map(seat -> {
                                    String status = soldSeatIds.contains(seat.getId()) ? "SOLD" : "AVAILABLE";
                                    return new SeatMapResponse.SeatMapSeat(
                                            seat.getId(),
                                            seat.label(),
                                            seat.getSeatType().name(),
                                            calculatePrice(showtime.getBasePrice(), seat.getSeatType()),
                                            status
                                    );
                                })
                                .toList()
                ))
                .toList();

        return new SeatMapResponse(
                showtime.getId(),
                SeatMapResponse.MovieSummary.from(movie),
                SeatMapResponse.CinemaSummary.from(cinema),
                SeatMapResponse.RoomSummary.from(room),
                showtime.getStartTime(),
                showtime.getFormat(),
                rows
        );
    }

    private Integer calculatePrice(Integer basePrice, com.cinebook.module.seat.entity.SeatType type) {
        return switch (type) {
            case VIP -> basePrice + 20000;
            case COUPLE -> basePrice + 50000;
            case WHEELCHAIR, STANDARD -> basePrice;
        };
    }

    @Transactional(readOnly = true)
    public Showtime findOrThrow(UUID id) {
        return showtimeRepository.findById(id)
                .orElseThrow(() -> new CinebookException(ErrorCode.SHOWTIME_NOT_FOUND));
    }
}
