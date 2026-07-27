package com.cinebook.module.movie.repository;

import com.cinebook.module.movie.entity.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.UUID;

public interface MovieRepository extends JpaRepository<Movie, UUID> {
    
    @Modifying
    @Query("UPDATE Movie m SET m.score = :score WHERE m.id = :movieId")
    void updateScore(@Param("movieId") UUID movieId, @Param("score") BigDecimal score);
}
