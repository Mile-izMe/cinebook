package com.cinebook.module.review.messaging;


import com.cinebook.module.movie.repository.MovieRepository;
import com.cinebook.module.review.event.MovieReviewedEvent;
import com.cinebook.module.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Worker that recomputes movie.score off the request path.
 * Idempotent by nature: re-running for the same movieId just recomputes the
 * same AVG(rating), no side effects accumulate.
 */
@Component
@RequiredArgsConstructor
public class MovieScoreConsumer {


    private static final Logger log = LoggerFactory.getLogger(MovieScoreConsumer.class);

    private final ReviewRepository reviewRepository;
    private final MovieRepository movieRepository;

    @RabbitListener(queues = "${rabbitmq.review.queue}")
    @Transactional
    public void onMovieReviewed(MovieReviewedEvent event) {
        BigDecimal rawAvg = reviewRepository.averageRatingByMovieId(event.movieId());

        if (rawAvg != null) {
            // MULTIPLY TO MAKE FROM 5 SCORE (User) TO 10 SCORE (Global/TMDB)
            BigDecimal normalizedAvg = rawAvg.multiply(new BigDecimal("2"));
            BigDecimal score = normalizedAvg.setScale(1, RoundingMode.HALF_UP);

            movieRepository.updateScore(event.movieId(), score);
            log.info("Recalculated score for movieId={} -> {}", event.movieId(), score);
        }
    }
}
