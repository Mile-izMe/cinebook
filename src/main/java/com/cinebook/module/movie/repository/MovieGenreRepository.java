package com.cinebook.module.movie.repository;

import com.cinebook.module.movie.entity.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MovieGenreRepository extends JpaRepository<MovieGenre, UUID> {

    List<MovieGenre> findAllByMovieId(UUID movieId);

    void deleteAllByMovieId(UUID movieId);
}
