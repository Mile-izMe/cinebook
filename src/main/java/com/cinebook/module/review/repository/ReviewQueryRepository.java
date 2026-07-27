package com.cinebook.module.review.repository;

import com.cinebook.common.util.CursorCodec;
import com.cinebook.module.review.entity.Review;
import jakarta.persistence.EntityManager;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


/**
 * Cursor pagination for GET /movies/{id}/reviews - same pattern as MovieQueryRepository.
 */
@Repository
@RequiredArgsConstructor
public class ReviewQueryRepository {

    private final EntityManager em;
    private final CursorCodec cursorCodec;

    public List<Review> findByMovie(UUID movieId, String cursor, int limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Review> query = cb.createQuery(Review.class);
        Root<Review> review = query.from(Review.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(review.get("movie").get("id"), movieId));
        predicates.add(cb.isNull(review.get("deletedAt")));

        Optional<CursorCodec.Cursor> decoded = cursorCodec.decode(cursor);
        decoded.ifPresent(c -> predicates.add(cb.or(
                cb.lessThan(review.get("createdAt"), c.createdAt()),
                cb.and(cb.equal(review.get("createdAt"), c.createdAt()),
                        cb.lessThan(review.get("id"), c.id()))
        )));

        query.where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(review.get("createdAt")), cb.desc(review.get("id")));

        return em.createQuery(query).setMaxResults(limit + 1).getResultList();
    }
}
