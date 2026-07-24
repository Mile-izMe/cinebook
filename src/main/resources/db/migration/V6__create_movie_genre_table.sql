CREATE TABLE movie_genre (
    id        UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    movie_id  UUID NOT NULL REFERENCES movie(id) ON DELETE CASCADE,
    genre_id  UUID NOT NULL REFERENCES genre(id),

    created_at TIMESTAMP NOT NULL DEFAULT now(),
    created_by UUID NULL,

    UNIQUE(movie_id, genre_id)
);

CREATE INDEX idx_movie_genre_movie_id ON movie_genre(movie_id);
CREATE INDEX idx_movie_genre_genre_id ON movie_genre(genre_id);
