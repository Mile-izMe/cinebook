package com.cinebook.module.seat.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.room.entity.Room;
import com.cinebook.module.room.service.RoomService;
import com.cinebook.module.seat.dto.request.GenerateSeatRequest;
import com.cinebook.module.seat.dto.response.SeatResponse;
import com.cinebook.module.seat.entity.Seat;
import com.cinebook.module.seat.entity.SeatType;
import com.cinebook.module.seat.mapper.SeatMapper;
import com.cinebook.module.seat.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatService {

    private final SeatRepository seatRepository;
    private final RoomService roomService;
    private final SeatMapper seatMapper;

    @Transactional(readOnly = true)
    public List<SeatResponse> listByRoom(UUID roomId) {
        return seatRepository.findAllByRoomIdOrderByRowAscNumberAsc(roomId).stream()
                .map(seatMapper::toResponse)
                .toList();
    }

    /*
     * "Server auto generate" instead admin have to POST each seat.
     */
    @Transactional
    public List<SeatResponse> generateSeats(UUID roomId, GenerateSeatRequest request) {
        Room room = roomService.findOrThrow(roomId);

        if (seatRepository.existsByRoomId(roomId)) {
            throw new CinebookException(ErrorCode.SEATS_ALREADY_GENERATED);
        }

        List<Seat> seats = new ArrayList<>();
        for (int r = 0; r < request.rows(); r++) {
            String rowLabel = String.valueOf((char) ('A' + r));
            for (int col = 1; col <= request.columns(); col++) {
                SeatType type = (r >= request.rows() / 2 && r <= request.rows() / 2 + 1)
                        ? SeatType.VIP : SeatType.STANDARD;

                seats.add(Seat.builder()
                        .room(room)
                        .row(rowLabel)
                        .number(col)
                        .seatType(type)
                        .build());
            }
        }

        return seatRepository.saveAll(seats).stream().map(seatMapper::toResponse).toList();
    }

    public Seat findOrThrow(UUID seatId) {
        return seatRepository.findById(seatId)
                .orElseThrow(() -> new CinebookException(ErrorCode.SEAT_NOT_FOUND));
    }
}
