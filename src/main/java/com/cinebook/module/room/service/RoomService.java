package com.cinebook.module.room.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.module.cinema.entity.Cinema;
import com.cinebook.module.cinema.service.CinemaService;
import com.cinebook.module.room.dto.request.RoomCreateRequest;
import com.cinebook.module.room.dto.response.RoomResponse;
import com.cinebook.module.room.entity.Room;
import com.cinebook.module.room.entity.RoomStatus;
import com.cinebook.module.room.mapper.RoomMapper;
import com.cinebook.module.room.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final CinemaService cinemaService;
    private final RoomMapper roomMapper;

    @Transactional(readOnly = true)
    public List<RoomResponse> listByCinema(UUID cinemaId) {
        return roomRepository.findAllByCinemaId(cinemaId).stream().map(roomMapper::toResponse).toList();
    }

    @Transactional
    public RoomResponse create(UUID cinemaId, RoomCreateRequest request) {
        Cinema cinema = cinemaService.findOrThrow(cinemaId);

        Room room = Room.builder()
                .cinema(cinema)
                .name(request.name())
                .capacity(request.capacity())
                .roomType(request.roomType())
                .status(RoomStatus.ACTIVE)
                .build();

        return roomMapper.toResponse(roomRepository.save(room));
    }

    Room findOrThrow(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new CinebookException(ErrorCode.ROOM_NOT_FOUND));
    }
}
