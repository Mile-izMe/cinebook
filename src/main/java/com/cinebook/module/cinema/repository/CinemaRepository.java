package com.cinebook.module.cinema.repository;

import com.cinebook.module.cinema.entity.Cinema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CinemaRepository extends JpaRepository<Cinema, UUID> {
    List<Cinema> findAllByCityId(UUID cityId);
}
