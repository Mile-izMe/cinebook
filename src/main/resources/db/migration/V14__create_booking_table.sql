CREATE TABLE booking (
    id                UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    showtime_id       UUID NOT NULL REFERENCES showtime(id),
    user_id           UUID NOT NULL REFERENCES "user"(id),
    snapshot          JSONB NULL,
    total_price       INT NOT NULL,
    status            VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    booking_time      TIMESTAMP NOT NULL DEFAULT now(),

    created_at        TIMESTAMP NOT NULL DEFAULT now(),
    created_by        UUID NULL,
    updated_at        TIMESTAMP NULL,
    updated_by        UUID NULL,
    deleted_at        TIMESTAMP NULL,
    deleted_by        UUID NULL
);

CREATE INDEX idx_booking_user_id ON booking(user_id, created_at DESC, id DESC);
CREATE INDEX idx_booking_showtime_id ON booking(showtime_id);