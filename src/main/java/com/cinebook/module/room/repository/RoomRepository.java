package com.cinebook.module.room.repository;

import com.cinebook.module.room.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findAllByCinemaId(UUID cinemaId);
}
