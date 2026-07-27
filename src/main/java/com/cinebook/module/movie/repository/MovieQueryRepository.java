package com.cinebook.module.movie.repository;

import com.cinebook.common.util.CursorCodec;
import com.cinebook.module.movie.entity.Movie;
import com.cinebook.module.movie.entity.MovieGenre;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Search.
 * Hand-rolled instead of Spring Data derived queries because cursor (keyset)
 * pagination combined with optional dynamic filters (keyword, genre) isn't
 * expressible with a simple @Query/Pageable - Pageable is OFFSET-based.
 */
@Repository
@RequiredArgsConstructor
public class MovieQueryRepository {

    private final EntityManager em;
    private final CursorCodec cursorCodec;

    public List<Movie> search(String keyword, UUID genreId, String cursor, int limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Movie> query = cb.createQuery(Movie.class);
        Root<Movie> movie = query.from(Movie.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.isNull(movie.get("deletedAt")));

        if (keyword != null && !keyword.isBlank()) {
            predicates.add(cb.like(cb.lower(movie.get("title")), "%" + keyword.toLowerCase() + "%"));
        }

        if (genreId != null) {
            Subquery<UUID> genreSub = query.subquery(UUID.class);
            Root<MovieGenre> mg = genreSub.from(MovieGenre.class);
            genreSub.select(mg.get("movie").get("id"))
                    .where(cb.equal(mg.get("genre").get("id"), genreId));
            predicates.add(movie.get("id").in(genreSub));
        }

        Optional<CursorCodec.Cursor> decoded = cursorCodec.decode(cursor);
        decoded.ifPresent(c -> {
            // keyset condition: (created_at, id) < (cursor.createdAt, cursor.id)
            // i.e. created_at < X, OR (created_at = X AND id < Y)
            Predicate before = cb.or(
                    cb.lessThan(movie.get("createdAt"), c.createdAt()),
                    cb.and(cb.equal(movie.get("createdAt"), c.createdAt()),
                            cb.lessThan(movie.get("id"), c.id()))
            );
            predicates.add(before);
        });

        query.where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(movie.get("createdAt")), cb.desc(movie.get("id")));

        return em.createQuery(query)
                .setMaxResults(limit + 1)
                .getResultList();
    }
}
