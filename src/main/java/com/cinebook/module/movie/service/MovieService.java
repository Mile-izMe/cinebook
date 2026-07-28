package com.cinebook.module.movie.service;

import com.cinebook.common.exception.CinebookException;
import com.cinebook.common.exception.ErrorCode;
import com.cinebook.common.util.CursorCodec;
import com.cinebook.common.util.CursorPageResponse;
import com.cinebook.module.movie.dto.request.MovieCreateRequest;
import com.cinebook.module.movie.dto.request.MovieUpdateRequest;
import com.cinebook.module.movie.dto.response.GenreResponse;
import com.cinebook.module.movie.dto.response.MovieDetailResponse;
import com.cinebook.module.movie.dto.response.MovieSummaryResponse;
import com.cinebook.module.movie.entity.Genre;
import com.cinebook.module.movie.entity.Movie;
import com.cinebook.module.movie.entity.MovieGenre;
import com.cinebook.module.movie.repository.GenreRepository;
import com.cinebook.module.movie.repository.MovieGenreRepository;
import com.cinebook.module.movie.repository.MovieQueryRepository;
import com.cinebook.module.movie.repository.MovieRepository;
import com.cinebook.module.review.dto.response.ReviewResponse;
import com.cinebook.module.review.entity.Review;
import com.cinebook.module.review.repository.ReviewRepository;
import com.cinebook.module.storage.service.MinioBuildService;
import com.cinebook.module.storage.service.MinioGetService;
import com.cinebook.module.storage.service.MinioWriteService;
import com.cinebook.module.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final MovieGenreRepository movieGenreRepository;
    private final MovieQueryRepository movieQueryRepository;
    private final ReviewRepository reviewRepository;
    private final GenreRepository genreRepository;
    private final CursorCodec cursorCodec;

    // ============ MINIO ================
    private final MinioGetService minioGetService;
    private final MinioBuildService minioBuildService;
    private final MinioWriteService minioWriteService;

    // -----------------------------------------------------------
    // Movie CRUD
    // -----------------------------------------------------------
    @Transactional
    public MovieSummaryResponse create(MovieCreateRequest request) {
        List<Genre> genres = genreRepository.findAllByIdIn(request.genreIds());
        if (genres.size() != request.genreIds().size()) {
            throw new CinebookException(ErrorCode.GENRE_NOT_FOUND);
        }

        Movie movie = Movie.builder()
                .title(request.title())
                .description(request.description())
                .duration(request.duration())
                .ageRating(request.ageRating())
                .releaseDate(request.releaseDate())
                .director(request.director())
                .cast(request.cast())
                .trailerUrl(request.trailerUrl())
                .build();

        // deleteObject(null) auto no-op
        applyImage(movie, request.posterObjectKey(), request.posterUrl(),
                movie::setPosterObjectKey, movie::setPosterUrl);
        applyImage(movie, request.backdropObjectKey(), request.backdropUrl(),
                movie::setBackdropObjectKey, movie::setBackdropUrl);

        movie = movieRepository.save(movie);

        attachGenres(movie, genres);

        return toSummary(movie, genres);
    }

    @Transactional
    public MovieSummaryResponse update(UUID movieId, MovieUpdateRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CinebookException(ErrorCode.MOVIE_NOT_FOUND));

        List<Genre> genres = genreRepository.findAllByIdIn(request.genreIds());
        if (genres.size() != request.genreIds().size()) {
            throw new CinebookException(ErrorCode.GENRE_NOT_FOUND);
        }

        movie.setTitle(request.title());
        movie.setDescription(request.description());
        movie.setDuration(request.duration());
        movie.setAgeRating(request.ageRating());
        movie.setReleaseDate(request.releaseDate());
        movie.setDirector(request.director());
        movie.setCast(request.cast());
        movie.setTrailerUrl(request.trailerUrl());
        movieRepository.save(movie);

        // Replace genre associations wholesale - simpler and safe at this scale
        // (a handful of rows per movie) vs. diffing add/remove sets.
        movieGenreRepository.deleteAllByMovieId(movieId);
        attachGenres(movie, genres);

        updateImageIfChanged(movie, request.posterObjectKey(), movie.getPosterObjectKey(),
                movie::setPosterObjectKey, movie::setPosterUrl);
        updateImageIfChanged(movie, request.backdropObjectKey(), movie.getBackdropObjectKey(),
                movie::setBackdropObjectKey, movie::setBackdropUrl);

        return toSummary(movie, genres);
    }

    @Transactional
    public void delete(UUID movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CinebookException(ErrorCode.MOVIE_NOT_FOUND));
        // Soft delete: rely on Auditable.deletedAt, not a real DELETE -
        // showtimes/bookings created against this movie must keep referring to it.
        movie.setDeletedAt(Instant.now());
        movieRepository.save(movie);
    }


    // -----------------------------------------------------------
    // Search (cursor pagination)
    // -----------------------------------------------------------
    @Transactional(readOnly = true)
    public CursorPageResponse<MovieSummaryResponse> search(String keyword, UUID genreId, String cursor, int limit) {
        List<Movie> rows = movieQueryRepository.search(keyword, genreId, cursor, limit);

        List<MovieSummaryResponse> items = rows.stream()
                .map(m -> toSummary(m, genresOf(m.getId())))
                .toList();

        return CursorPageResponse.of(items, limit, cursorCodec,
                item -> {
                    // re-fetch createdAt/id pairing from the row list by matching id,
                    // cheaper alternative: keep a parallel map; fine at this list size.
                    Movie match = rows.stream().filter(r -> r.getId().equals(item.id())).findFirst().orElseThrow();
                    return new CursorCodec.Cursor(match.getCreatedAt(), match.getId());
                });
    }

    // -----------------------------------------------------------
    // Movie detail (most important API of the module)
    // -----------------------------------------------------------
    @Transactional(readOnly = true)
    public MovieDetailResponse getDetail(UUID movieId) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new CinebookException(ErrorCode.MOVIE_NOT_FOUND));

        List<GenreResponse> genres = genresOf(movieId).stream()
                .map(g -> new GenreResponse(g.getId(), g.getName()))
                .toList();

        long totalReviews = reviewRepository.countByMovieId(movieId);

        List<ReviewResponse> recentReviews = reviewRepository
                .findTop5ByMovieIdOrderByCreatedAtDesc(movieId).stream()
                .map(this::toReviewResponse)
                .toList();

        return MovieDetailResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .posterUrl(movie.getPosterUrl())
                .backdropUrl(movie.getBackdropUrl())
                .trailerUrl(movie.getTrailerUrl())
                .duration(movie.getDuration())
                .ageRating(movie.getAgeRating())
                .score(movie.getScore())
                .totalReviews(totalReviews)
                .releaseDate(movie.getReleaseDate())
                .director(movie.getDirector())
                .cast(movie.getCast())
                .genres(genres)
                .recentReviews(recentReviews)
                .build();
    }

    // -----------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------
    private void attachGenres(Movie movie, List<Genre> genres) {
        List<MovieGenre> links = genres.stream()
                .map(g -> MovieGenre.builder().movie(movie).genre(g).build())
                .toList();
        movieGenreRepository.saveAll(links);
    }

    private List<Genre> genresOf(UUID movieId) {
        return movieGenreRepository.findAllByMovieId(movieId).stream()
                .map(MovieGenre::getGenre)
                .toList();
    }

    private MovieSummaryResponse toSummary(Movie movie, List<Genre> genres) {
        return MovieSummaryResponse.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .posterUrl(movie.getPosterUrl())
                .backdropUrl(movie.getBackdropUrl())
                .duration(movie.getDuration())
                .score(movie.getScore())
                .ageRating(movie.getAgeRating())
                .genres(genres.stream()
                        .map(g -> new GenreResponse(g.getId(), g.getName()))
                        .toList())
                .build();
    }

    private ReviewResponse toReviewResponse(Review review) {
        User user = review.getUser();

        return ReviewResponse.builder()
                .id(review.getId())
                .userId(user.getId())
                .userName(user.getUserName())
                .userAvatarUrl(user.getAvatarUrl())
                .rating(review.getRating())
                .comment(review.getComment())
                .createdAt(review.getCreatedAt())
                .build();
    }

    private void updateImageIfChanged(Movie movie, String newObjectKey, String oldObjectKey,
                                      Consumer<String> setObjectKey, Consumer<String> setUrl) {
        if (newObjectKey == null || newObjectKey.equals(oldObjectKey)) return;

        minioGetService.requireObjectExists(newObjectKey);
        setObjectKey.accept(newObjectKey);
        setUrl.accept(minioBuildService.buildPublicUrl(newObjectKey));
        minioWriteService.deleteObject(oldObjectKey);
    }

    /**
     * Used at CREATE time only: prefer a real uploaded object (MinIO, via presign
     * flow), fall back to a raw external URL (e.g. seeded from TMDB) when no
     * objectKey is present. Never both - objectKey wins if both happen to be sent.
     */
    private void applyImage(Movie movie, String objectKey, String externalUrl,
                            Consumer<String> setObjectKey, Consumer<String> setUrl) {
        if (objectKey != null) {
            minioGetService.requireObjectExists(objectKey);
            setObjectKey.accept(objectKey);
            setUrl.accept(minioBuildService.buildPublicUrl(objectKey));
        } else if (externalUrl != null) {
            setUrl.accept(externalUrl); // no objectKey to track - nothing to clean up in MinIO later
        }
    }
}
