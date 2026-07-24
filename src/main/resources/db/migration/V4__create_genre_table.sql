CREATE TABLE genre (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(50) NOT NULL UNIQUE,

    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    created_by  UUID NULL,
    updated_at  TIMESTAMP NULL,
    updated_by  UUID NULL,
    deleted_at  TIMESTAMP NULL,
    deleted_by  UUID NULL
);

INSERT INTO genre (id, name, created_at) VALUES
    (gen_random_uuid(), 'Action',  now()),
    (gen_random_uuid(), 'Drama',   now()),
    (gen_random_uuid(), 'Comedy',  now()),
    (gen_random_uuid(), 'Sci-Fi',  now()),
    (gen_random_uuid(), 'Horror',  now()),
    (gen_random_uuid(), 'Romance', now()),
    (gen_random_uuid(), 'Animation', now());
