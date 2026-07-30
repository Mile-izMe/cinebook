CREATE TABLE showtime (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    movie_id        UUID NOT NULL REFERENCES movie(id),
    room_id         UUID NOT NULL REFERENCES room(id),
    start_time      TIMESTAMP NOT NULL,
    end_time        TIMESTAMP NOT NULL,
    format          VARCHAR(10) NOT NULL DEFAULT '2D', -- 2D, 3D, IMAX
    base_price      INT NOT NULL,

    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    created_by   UUID NULL,
    updated_at   TIMESTAMP NULL,
    updated_by   UUID NULL,
    deleted_at   TIMESTAMP NULL,
    deleted_by   UUID NULL,

    UNIQUE(room_id, start_time) -- 1 ROOM CANNOT HAVE 2 SHOWTIME AT THE SAME START TIME
);

CREATE INDEX idx_showtime_movie_id ON showtime(movie_id);
CREATE INDEX idx_showtime_room_id ON showtime(room_id);
CREATE INDEX idx_showtime_start_time ON showtime(start_time);