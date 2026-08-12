CREATE TABLE payment (
    id                       UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    booking_id               UUID NOT NULL REFERENCES booking(id), -- 1:N, NOT unique
    amount                   INT NOT NULL,
    payment_method           VARCHAR(20) NOT NULL,  -- VNPAY, MOMO, MOCK
    provider                 VARCHAR(50),
    provider_transaction_id  VARCHAR(255),
    status                   VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    paid_at                  TIMESTAMP NULL,

    created_at               TIMESTAMP NOT NULL DEFAULT now(),
    created_by                UUID NULL,
    updated_at               TIMESTAMP NULL,
    updated_by               UUID NULL,
    deleted_at               TIMESTAMP NULL,
    deleted_by               UUID NULL
);

CREATE INDEX idx_payment_booking_id ON payment(booking_id);
CREATE UNIQUE INDEX idx_payment_provider_txn ON payment(provider_transaction_id)
    WHERE provider_transaction_id IS NOT NULL;