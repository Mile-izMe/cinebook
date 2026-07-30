CREATE TABLE city (
    id          UUID PRIMARY KEY,
    city_name   VARCHAR(50) NOT NULL,

    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    created_by  UUID NULL,
    updated_at  TIMESTAMP NULL,
    updated_by  UUID NULL,
    deleted_at  TIMESTAMP NULL,
    deleted_by  UUID NULL
);

INSERT INTO city (id, city_name, created_at) VALUES
(gen_random_uuid(), 'Ho Chi Minh City', now()),
(gen_random_uuid(), 'Ha Noi', now()),
(gen_random_uuid(), 'Da Nang', now());