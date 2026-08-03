CREATE TABLE booking_seat (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id     UUID NOT NULL REFERENCES booking(id),
    seat_id        UUID NOT NULL REFERENCES seat(id),
    showtime_id    UUID NOT NULL REFERENCES showtime(id),
    seat_label     VARCHAR(10) NOT NULL,
    price_snapshot INT NOT NULL,

    created_at     TIMESTAMP NOT NULL DEFAULT now(),
    created_by     UUID NULL,

    UNIQUE(booking_id, seat_id)
);

CREATE INDEX idx_booking_seat_booking_id ON booking_seat(booking_id);
-- Index (NOT UNIQUE) ON (showtime_id, seat_id) - SUPPORT query check
-- "SEAT BOOKED OR NOT", CAN NOT BLOCK REAL RACE CONDITION
CREATE INDEX idx_booking_seat_showtime_seat ON booking_seat(showtime_id, seat_id);