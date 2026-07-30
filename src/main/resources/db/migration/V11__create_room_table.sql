CREATE TABLE room (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    cinema_id    UUID NOT NULL REFERENCES cinema(id),
    name         VARCHAR(50) NOT NULL,
    capacity     INT NOT NULL,
    room_type    VARCHAR(20) NOT NULL DEFAULT 'STANDARD', -- STANDARD, IMAX, 4DX
    status       VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',   -- ACTIVE, MAINTENANCE

    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    created_by   UUID NULL,
    updated_at   TIMESTAMP NULL,
    updated_by   UUID NULL,
    deleted_at   TIMESTAMP NULL,
    deleted_by   UUID NULL,

    UNIQUE(cinema_id, name)
);

CREATE INDEX idx_room_cinema_id ON room(cinema_id);