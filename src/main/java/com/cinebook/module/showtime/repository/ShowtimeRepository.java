package com.cinebook.module.showtime.repository;

import com.cinebook.module.showtime.entity.Showtime;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ShowtimeRepository extends JpaRepository<Showtime, UUID> {

    /**
     * cinemaId/date optional - use COALESCE trick to let 1 query handle
     * all contains filter or not, no need to build active query.
     */
    @Query("""
            SELECT s FROM Showtime s
            WHERE s.movie.id = :movieId
              AND s.deletedAt IS NULL
              AND (:cinemaId IS NULL OR s.room.cinema.id = :cinemaId)
              AND (:date IS NULL OR CAST(s.startTime AS date) = :date)
            ORDER BY s.startTime ASC
            """)
    List<Showtime> findByMovieAndFilters(@Param("movieId") UUID movieId,
                                         @Param("cinemaId") UUID cinemaId,
                                         @Param("date") LocalDate date);

    boolean existsByRoomIdAndStartTimeBetween(UUID roomId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
