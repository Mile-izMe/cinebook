CREATE TABLE refresh_token (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID NOT NULL REFERENCES "user"(id),
    device_id    VARCHAR(255) NOT NULL,
    token_hash   VARCHAR(255) NOT NULL,
    expires_at   TIMESTAMP NOT NULL,
    revoked_at   TIMESTAMP NULL,

    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    created_by   UUID NULL,
    updated_at   TIMESTAMP NULL,
    updated_by   UUID NULL,

    UNIQUE(user_id, device_id)
);

CREATE INDEX idx_refresh_token_user_id ON refresh_token(user_id);
CREATE INDEX idx_refresh_token_hash ON refresh_token(token_hash);