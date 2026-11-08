-- Keep existing airport records unchanged when the service restarts.
INSERT INTO airports (id, code, name, city, country)
VALUES
    ('a1000000-0000-4000-8000-000000000001', 'HAN', 'Nội Bài', 'Hà Nội', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000002', 'SGN', 'Tân Sơn Nhất', 'TP. Hồ Chí Minh', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000003', 'DAD', 'Đà Nẵng', 'Đà Nẵng', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000004', 'CXR', 'Cam Ranh', 'Nha Trang', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000005', 'PQC', 'Phú Quốc', 'Phú Quốc', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000006', 'DLI', 'Liên Khương', 'Đà Lạt', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000007', 'HUI', 'Phú Bài', 'Huế', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000008', 'VDO', 'Vân Đồn', 'Quảng Ninh', 'Vietnam'),
    ('a1000000-0000-4000-8000-000000000009', 'BKK', 'Suvarnabhumi', 'Bangkok', 'Thailand'),
    ('a1000000-0000-4000-8000-000000000010', 'SIN', 'Changi', 'Singapore', 'Singapore')
ON CONFLICT (code) DO NOTHING;

-- Demo configurations, not manufacturer capacity specifications.
-- Stable IDs make this seed idempotent without making model names unique.
INSERT INTO aircrafts (id, model, total_seats)
VALUES
    ('a2000000-0000-4000-8000-000000000001', 'Airbus A320', 180),
    ('a2000000-0000-4000-8000-000000000002', 'Airbus A321', 220),
    ('a2000000-0000-4000-8000-000000000003', 'Boeing 787', 294),
    ('a2000000-0000-4000-8000-000000000004', 'ATR 72', 72)
ON CONFLICT (id) DO NOTHING;

-- Dates are relative to the first startup (Vietnam local time). Existing schedules
-- are never moved on later startups. Default UI searches use +7 days / +13 days.
WITH samples (number, flight_code, origin_code, destination_code, day_offset, local_time, aircraft_number) AS (
    VALUES
        (1, 'VN247', 'HAN', 'SGN', 7, TIME '06:15', 2),
        (2, 'VJ129', 'HAN', 'SGN', 7, TIME '09:45', 1),
        (3, 'QH221', 'HAN', 'SGN', 7, TIME '13:20', 1),
        (4, 'VN261', 'HAN', 'SGN', 7, TIME '17:10', 3),
        (5, 'VN248', 'SGN', 'HAN', 13, TIME '07:00', 2),
        (6, 'VJ130', 'SGN', 'HAN', 13, TIME '10:30', 1),
        (7, 'QH222', 'SGN', 'HAN', 13, TIME '14:00', 1),
        (8, 'VN262', 'SGN', 'HAN', 13, TIME '18:00', 3),
        (9, 'VN171', 'HAN', 'DAD', 7, TIME '08:00', 2),
        (10, 'VN172', 'DAD', 'HAN', 13, TIME '15:00', 2),
        (11, 'VJ451', 'HAN', 'PQC', 7, TIME '09:00', 1),
        (12, 'VJ452', 'PQC', 'HAN', 13, TIME '16:00', 1),
        (13, 'QH301', 'HAN', 'DLI', 7, TIME '11:00', 1),
        (14, 'QH302', 'DLI', 'HAN', 13, TIME '17:00', 1)
), schedules AS (
    SELECT samples.*,
        (((CURRENT_TIMESTAMP AT TIME ZONE 'Asia/Ho_Chi_Minh')::date + day_offset + local_time)
            AT TIME ZONE 'Asia/Ho_Chi_Minh') AS departure,
        ('a2000000-0000-4000-8000-' || lpad(aircraft_number::text, 12, '0'))::uuid AS aircraft
    FROM samples
)
INSERT INTO flights (id, flight_code, origin_code, destination_code, departure_time, arrival_time, aircraft_id, status)
SELECT ('a3000000-0000-4000-8000-' || lpad(number::text, 12, '0'))::uuid,
    flight_code, origin_code, destination_code, departure, departure + INTERVAL '2 hours 10 minutes',
    schedules.aircraft, 'SCHEDULED'
FROM schedules
JOIN airports origin ON origin.code = schedules.origin_code
JOIN airports destination ON destination.code = schedules.destination_code
JOIN aircrafts aircraft ON aircraft.id = schedules.aircraft
ON CONFLICT (id) DO NOTHING;

-- Fares belong only to the seeded flights; class intervals do not overlap.
WITH classes (number, fare_class, multiplier) AS (
    VALUES (1, 'ECONOMY', 1.0), (2, 'BUSINESS', 2.0), (3, 'FIRST', 3.0)
)
INSERT INTO fares (id, flight_id, fare_class, base_price, currency, valid_from, valid_to)
SELECT ('a4000000-0000-4000-8000-' || lpad((right(flight.id::text, 12)::bigint * 10 + classes.number)::text, 12, '0'))::uuid,
    flight.id, classes.fare_class,
    (CASE WHEN flight.flight_code LIKE 'VJ%' THEN 890000
          WHEN flight.flight_code LIKE 'QH%' THEN 1150000 ELSE 1290000 END) * classes.multiplier,
    'VND', CURRENT_TIMESTAMP - INTERVAL '1 day', flight.departure_time
FROM flights flight CROSS JOIN classes
WHERE flight.id BETWEEN 'a3000000-0000-4000-8000-000000000001'::uuid AND 'a3000000-0000-4000-8000-000000000014'::uuid
    AND flight.departure_time > CURRENT_TIMESTAMP
    AND NOT EXISTS (
        SELECT 1 FROM fares existing
        WHERE existing.flight_id = flight.id AND existing.fare_class = classes.fare_class
            AND existing.valid_from < flight.departure_time
            AND existing.valid_to > CURRENT_TIMESTAMP - INTERVAL '1 day'
    )
ON CONFLICT (id) DO NOTHING;

-- A small demo seat catalog for each seeded flight: up to 24 seats, never above
-- aircraft capacity. Existing seat numbers, statuses, and booking references remain unchanged.
WITH candidates AS (
    SELECT flight.id AS flight_id, aircraft.total_seats, sample.seat_index,
        ('a5000000-0000-4000-8000-' || lpad((right(flight.id::text, 12)::bigint * 100 + sample.seat_index)::text, 12, '0'))::uuid AS id,
        (((sample.seat_index - 1) / 6 + 1)::text || chr(65 + (sample.seat_index - 1) % 6)) AS seat_number,
        CASE WHEN sample.seat_index <= 6 THEN 'FIRST'
             WHEN sample.seat_index <= 12 THEN 'BUSINESS' ELSE 'ECONOMY' END AS seat_class
    FROM flights flight
    JOIN aircrafts aircraft ON aircraft.id = flight.aircraft_id
    CROSS JOIN generate_series(1, 24) AS sample(seat_index)
    WHERE flight.id BETWEEN 'a3000000-0000-4000-8000-000000000001'::uuid AND 'a3000000-0000-4000-8000-000000000014'::uuid
), missing AS (
    SELECT candidate.*,
        ROW_NUMBER() OVER (PARTITION BY flight_id ORDER BY seat_index) AS position,
        total_seats - (SELECT count(*) FROM seats assigned WHERE assigned.flight_id = candidate.flight_id) AS remaining
    FROM candidates candidate
    WHERE NOT EXISTS (SELECT 1 FROM seats existing WHERE existing.flight_id = candidate.flight_id AND existing.seat_number = candidate.seat_number)
      AND NOT EXISTS (SELECT 1 FROM seats existing WHERE existing.id = candidate.id)
)
INSERT INTO seats (id, flight_id, seat_number, seat_class, status, held_by_booking_id, hold_expires_at)
SELECT id, flight_id, seat_number, seat_class,
    CASE WHEN seat_index = 24 THEN 'BLOCKED' ELSE 'AVAILABLE' END, NULL, NULL
FROM missing
WHERE position <= remaining
ON CONFLICT DO NOTHING;
