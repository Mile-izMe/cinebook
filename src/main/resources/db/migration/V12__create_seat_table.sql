CREATE TABLE seat (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    room_id         UUID NOT NULL REFERENCES room(id),
    "row"           VARCHAR(10) NOT NULL,
    number          INT NOT NULL,
    seat_type       VARCHAR(20) NOT NULL DEFAULT 'STANDARD', -- STANDARD, VIP, WHEELCHAIR, COUPLE
--     price_modifier  INT NOT NULL DEFAULT 0,

    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    created_by   UUID NULL,
    updated_at   TIMESTAMP NULL,
    updated_by   UUID NULL,
    deleted_at   TIMESTAMP NULL,
    deleted_by   UUID NULL,

    UNIQUE(room_id, "row", number)
);

CREATE INDEX idx_seat_room_id ON seat(room_id);