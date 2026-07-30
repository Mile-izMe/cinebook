package com.cinebook.module.seat.repository;

import com.cinebook.module.seat.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SeatRepository extends JpaRepository<Seat, UUID> {

    List<Seat> findAllByRoomIdOrderByRowAscNumberAsc(UUID roomId);

    boolean existsByRoomId(UUID roomId);
}
