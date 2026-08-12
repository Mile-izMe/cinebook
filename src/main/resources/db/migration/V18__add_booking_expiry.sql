ALTER TABLE booking ADD COLUMN expires_at TIMESTAMP;
CREATE INDEX idx_booking_pending_expiry ON booking(status, expires_at) WHERE status = 'PENDING';