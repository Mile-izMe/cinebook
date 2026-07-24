CREATE TABLE review (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    movie_id    UUID NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    user_id     UUID NOT NULL REFERENCES "user"(id),
    rating      DECIMAL(2,1) NOT NULL CHECK (rating >= 1 AND rating <= 5),
    comment     TEXT,

    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    created_by  UUID NULL,
    updated_at  TIMESTAMP NULL,
    updated_by  UUID NULL,
    deleted_at  TIMESTAMP NULL,
    deleted_by  UUID NULL,

    UNIQUE(movie_id, user_id)
);

CREATE INDEX idx_review_movie_created_at ON review(movie_id, created_at DESC, id DESC);
