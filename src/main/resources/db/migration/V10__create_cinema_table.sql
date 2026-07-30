CREATE TABLE cinema (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    city_id      UUID NOT NULL REFERENCES city(id),
    name         VARCHAR(100) NOT NULL,
    address      VARCHAR(255) NOT NULL,
    latitude     DECIMAL(9,6),
    longitude    DECIMAL(9,6),

    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    created_by   UUID NULL,
    updated_at   TIMESTAMP NULL,
    updated_by   UUID NULL,
    deleted_at   TIMESTAMP NULL,
    deleted_by   UUID NULL
);

CREATE INDEX idx_cinema_city_id ON cinema(city_id);