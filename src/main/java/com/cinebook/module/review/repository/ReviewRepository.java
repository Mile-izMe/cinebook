package com.cinebook.module.review.repository;

import com.cinebook.module.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {

    boolean existsByMovieIdAndUserId(UUID movieId, UUID userId);

    long countByMovieId(UUID movieId);

    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.movie.id = :movieId")
    BigDecimal averageRatingByMovieId(@Param("movieId") UUID movieId);

    /**
     * Small preview for MovieDetailResponse - see Milestone 3.5.
     */
    List<Review> findTop5ByMovieIdOrderByCreatedAtDesc(UUID movieId);
}
