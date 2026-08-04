ALTER TABLE booking
    ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE booking
    ADD COLUMN guest_email VARCHAR(255);
ALTER TABLE booking
    ADD COLUMN guest_phone VARCHAR(20);
ALTER TABLE booking
    ADD COLUMN booking_code VARCHAR(10) UNIQUE;
-- Lookup code for guest, E.g: "CB-7X9K2"

-- Required: Each booking has its own owner - USER or GUEST
ALTER TABLE booking
    ADD CONSTRAINT chk_booking_owner
        CHECK (user_id IS NOT NULL OR (guest_email IS NOT NULL AND guest_phone IS NOT NULL));