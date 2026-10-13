INSERT INTO properties
(
    name,
    location,
    description,
    max_guests,
    base_nightly_rate,
    currency,
    minimum_stay_nights,
    check_in_time,
    check_out_time,
    time_zone,
    cancellation_notice_days,
    active
)
VALUES
(
    'Villa Tusia',
    'Greece',
    'Vacation home with sea view, approximately 500 meters from the sea.',
    2,
    100.00,
    'EUR',
    1,
    TIME '15:00',
    TIME '11:00',
    'Europe/Athens',
    14,
    TRUE
);
