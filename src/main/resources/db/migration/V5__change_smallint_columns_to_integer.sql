ALTER TABLE properties
    ALTER COLUMN max_guests TYPE INTEGER;

ALTER TABLE properties
    ALTER COLUMN minimum_stay_nights TYPE INTEGER;

ALTER TABLE reservations
    ALTER COLUMN guest_count TYPE INTEGER;
