package com.cinebook.module.movie.entity;

import com.cinebook.common.entity.Auditable;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "movie")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Movie extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(name = "poster_url")
    private String posterUrl;

    @Column(name = "backdrop_url")
    private String backdropUrl;

    @Column(name = "trailer_url")
    private String trailerUrl;

    @Column(name = "duration", nullable = false)
    private Integer duration; // minutes

    @Column(name = "age_rating", nullable = false, length = 10)
    private String ageRating;

    /**
     * Cached avg(review.rating). Recalculated asynchronously via Queue.
     */
    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "director", nullable = false)
    private String director;

    @Column(name = "\"cast\"", nullable = false) // "cast" is a reserved word in Postgres
    @JdbcTypeCode(SqlTypes.ARRAY)
    private List<String> cast;
}
