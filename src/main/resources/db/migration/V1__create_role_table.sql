CREATE TABLE role (
    id          UUID PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL,
    description VARCHAR(50) NOT NULL,

    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    created_by  UUID NULL,
    updated_at  TIMESTAMP NULL,
    updated_by  UUID NULL,
    deleted_at  TIMESTAMP NULL,
    deleted_by  UUID NULL
);