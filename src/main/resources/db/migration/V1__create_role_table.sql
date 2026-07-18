CREATE TABLE role (
    id          UUID PRIMARY KEY,
    role_name   VARCHAR(50) NOT NULL,
    role_code   VARCHAR(50) NOT NULL,

    created_at  TIMESTAMP NOT NULL DEFAULT now(),
    created_by  UUID NULL,
    updated_at  TIMESTAMP NULL,
    updated_by  UUID NULL,
    deleted_at  TIMESTAMP NULL,
    deleted_by  UUID NULL
);

INSERT INTO role (id, role_name, role_code, created_at) VALUES
    (gen_random_uuid(), 'Administrator', 'ADMIN',    now()),
    (gen_random_uuid(), 'Staff',         'STAFF',    now()),
    (gen_random_uuid(), 'Customer',      'CUSTOMER', now());