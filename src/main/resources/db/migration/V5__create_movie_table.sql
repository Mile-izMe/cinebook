CREATE TABLE movie (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title        VARCHAR(255) NOT NULL,
    description  TEXT NOT NULL,
    poster_url   VARCHAR(500),
    backdrop_url VARCHAR(500),
    trailer_url  VARCHAR(500),
    duration     INT NOT NULL,               -- minutes
    age_rating   VARCHAR(10) NOT NULL,        -- P, K, T13, T16, T18...
    score        DECIMAL(3,1),                -- cached avg(review.rating), NULL until first review
    release_date DATE NOT NULL,
    director     VARCHAR(255) NOT NULL,
    "cast"       TEXT[] NOT NULL DEFAULT '{}',

    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    created_by   UUID NULL,
    updated_at   TIMESTAMP NULL,
    updated_by   UUID NULL,
    deleted_at   TIMESTAMP NULL,
    deleted_by   UUID NULL
);

-- Cursor pagination on (created_at, id) - see Milestone 3.3
CREATE INDEX idx_movie_created_at_id ON movie(created_at DESC, id DESC);
CREATE INDEX idx_movie_title ON movie USING gin (to_tsvector('simple', title));
