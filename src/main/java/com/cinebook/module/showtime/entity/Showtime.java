package com.cinebook.module.showtime.entity;

import com.cinebook.common.entity.Auditable;
import com.cinebook.module.movie.entity.Movie;
import com.cinebook.module.room.entity.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "showtime", uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "start_time"}))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Showtime extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "format", nullable = false, length = 10)
    private String format;

    @Column(name = "base_price", nullable = false)
    private Integer basePrice;
}
