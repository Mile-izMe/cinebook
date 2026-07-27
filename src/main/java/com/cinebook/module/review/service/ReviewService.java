package com.cinebook.module.review.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.common.util.CursorCodec;
import com.cinebook.common.util.CursorPageResponse;
import com.cinebook.module.movie.entity.Movie;
import com.cinebook.module.movie.repository.MovieRepository;
import com.cinebook.module.review.dto.request.ReviewRequest;
import com.cinebook.module.review.dto.response.ReviewResponse;
import com.cinebook.module.review.entity.Review;
import com.cinebook.module.review.event.MovieReviewedEvent;
import com.cinebook.module.review.mapper.ReviewMapper;
import com.cinebook.module.review.messaging.ReviewEventPublisher;
import com.cinebook.module.review.repository.ReviewQueryRepository;
import com.cinebook.module.review.repository.ReviewRepository;
import com.cinebook.module.user.entity.User;
import com.cinebook.module.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewQueryRepository reviewQueryRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final ReviewEventPublisher reviewEventPublisher;
    private final CursorCodec cursorCodec;
    private final ReviewMapper reviewMapper;

    @Transactional
    public ReviewResponse create(UUID movieId, UUID userId, ReviewRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CinebookException(ErrorCode.MOVIE_NOT_FOUND));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CinebookException(ErrorCode.USER_NOT_FOUND));

        // Rely on DB UNIQUE(movie_id, user_id) as the real guard against a
        // race between two concurrent submits; this is just a friendlier error.
        if (reviewRepository.existsByMovieIdAndUserId(movieId, userId)) {
            throw new CinebookException(ErrorCode.REVIEW_ALREADY_EXISTS);
        }

        Review review = Review.builder()
                .movie(movie)
                .user(user)
                .rating(request.rating())
                .comment(request.comment())
                .build();

        review = reviewRepository.save(review);

        // Enqueue only - never recompute AVG(rating) synchronously in this request.
        reviewEventPublisher.publishMovieReviewed(new MovieReviewedEvent(movieId));

        return reviewMapper.toResponse(review);
    }

    @Transactional(readOnly = true)
    public CursorPageResponse<ReviewResponse> list(UUID movieId, String cursor, int limit) {
        List<Review> rows = reviewQueryRepository.findByMovie(movieId, cursor, limit);
        List<ReviewResponse> items = rows.stream().map(reviewMapper::toResponse).toList();

        return CursorPageResponse.of(items, limit, cursorCodec,
                r -> new CursorCodec.Cursor(r.createdAt(), r.id()));
    }

}
