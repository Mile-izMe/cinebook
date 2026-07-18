CREATE TABLE "user" (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    role_id      UUID NOT NULL REFERENCES role(id),
    user_name    VARCHAR(100) NOT NULL,
    password     VARCHAR(255) NOT NULL,
    email        VARCHAR(255) NOT NULL UNIQUE,
    phone        VARCHAR(20)  NOT NULL UNIQUE,
    avatar_url   VARCHAR(500),
    is_verified  BOOLEAN NOT NULL DEFAULT false,

    created_at   TIMESTAMP NOT NULL DEFAULT now(),
    created_by   UUID NULL,
    updated_at   TIMESTAMP NULL,
    updated_by   UUID NULL,
    deleted_at   TIMESTAMP NULL,
    deleted_by   UUID NULL
);

CREATE INDEX idx_user_role_id ON "user"(role_id);