package com.cinebook.module.booking.repository;

import com.cinebook.common.util.CursorCodec;
import com.cinebook.module.booking.entity.Booking;
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

@Repository
@RequiredArgsConstructor
public class BookingQueryRepository {

    private final EntityManager em;
    private final CursorCodec cursorCodec;

    public List<Booking> findByUser(UUID userId, String cursor, int limit) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Booking> query = cb.createQuery(Booking.class);
        Root<Booking> booking = query.from(Booking.class);

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(cb.equal(booking.get("user").get("id"), userId));
        predicates.add(cb.isNull(booking.get("deletedAt")));

        Optional<CursorCodec.Cursor> decoded = cursorCodec.decode(cursor);
        decoded.ifPresent(c -> predicates.add(cb.or(
                cb.lessThan(booking.get("createdAt"), c.createdAt()),
                cb.and(cb.equal(booking.get("createdAt"), c.createdAt()),
                        cb.lessThan(booking.get("id"), c.id()))
        )));

        query.where(predicates.toArray(new Predicate[0]))
                .orderBy(cb.desc(booking.get("createdAt")), cb.desc(booking.get("id")));

        return em.createQuery(query)
                .setMaxResults(limit + 1)
                .getResultList();
    }
}
