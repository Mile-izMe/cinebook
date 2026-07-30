package com.cinebook.module.room.entity;

import com.cinebook.common.entity.Auditable;
import com.cinebook.module.cinema.entity.Cinema;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "room", uniqueConstraints = @UniqueConstraint(columnNames = {"cinema_id", "name"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Room extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cinema_id", nullable = false)
    private Cinema cinema;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "capacity", nullable = false)
    private Integer capacity;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private RoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private RoomStatus status;
}
