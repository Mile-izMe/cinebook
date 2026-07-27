package com.cinebook.module.movie.repository;

import com.cinebook.module.movie.entity.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GenreRepository extends JpaRepository<Genre, UUID> {

    List<Genre> findAllByIdIn(List<UUID> ids);
    
}
