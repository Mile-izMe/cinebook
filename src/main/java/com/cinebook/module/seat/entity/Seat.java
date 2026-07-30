package com.cinebook.module.seat.entity;

import com.cinebook.common.entity.Auditable;
import com.cinebook.module.room.entity.Room;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "seat", uniqueConstraints = @UniqueConstraint(columnNames = {"room_id", "row", "number"}))
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
public class Seat extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    private Room room;

    @Column(name = "\"row\"", nullable = false, length = 10)
    private String row;

    @Column(name = "number", nullable = false)
    private Integer number;

    @Enumerated(EnumType.STRING)
    @Column(name = "seat_type", nullable = false, length = 20)
    private SeatType seatType;

    public String label() {
        return row + number;
    }
}
