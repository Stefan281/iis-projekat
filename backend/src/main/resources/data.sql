ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE reservations DROP CONSTRAINT IF EXISTS reservations_status_check;
ALTER TABLE tickets DROP CONSTRAINT IF EXISTS tickets_status_check;
ALTER TABLE seats DROP CONSTRAINT IF EXISTS seats_status_check;

DELETE FROM reservations;
DELETE FROM tickets;
DELETE FROM seats;
DELETE FROM zones;

INSERT INTO zones (id, name, description, price_coefficient, capacity, occupied_seats, occupancy_rate)
VALUES
    (1, 'North', 'North stand', 1.00, 40, 0, 0.00),
    (2, 'West', 'West stand', 1.20, 40, 0, 0.00),
    (3, 'East', 'East stand', 1.20, 40, 0, 0.00),
    (4, 'South', 'South stand', 1.00, 40, 0, 0.00)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    price_coefficient = EXCLUDED.price_coefficient,
    capacity = EXCLUDED.capacity,
    occupied_seats = EXCLUDED.occupied_seats,
    occupancy_rate = EXCLUDED.occupancy_rate;

SELECT setval(pg_get_serial_sequence('zones', 'id'), COALESCE((SELECT MAX(id) FROM zones), 1), true);

INSERT INTO matches (id, date, time, home_team, away_team, location, status, base_price, attractiveness, expected_attendance)
VALUES
    (1, '2026-06-15', '19:00:00', 'Vojvodina', 'Crvena zvezda', 'SPC Vojvodina', 'SCHEDULED', 900.00, 'HIGH', 500),
    (2, '2026-06-22', '20:00:00', 'Partizan', 'Radnicki', 'Hala Pionir', 'SCHEDULED', 1200.00, 'DERBY', 800)
ON CONFLICT (id) DO NOTHING;

UPDATE matches
SET home_team = 'Vojvodina',
    away_team = 'Crvena zvezda',
    location = 'SPC Vojvodina',
    status = 'SCHEDULED',
    base_price = 900.00,
    attractiveness = 'HIGH'
WHERE id = 1;

UPDATE matches
SET home_team = 'Partizan',
    away_team = 'Radnicki',
    location = 'Hala Pionir',
    status = 'SCHEDULED',
    base_price = 1200.00,
    attractiveness = 'DERBY'
WHERE id = 2;

UPDATE matches
SET status = 'SCHEDULED'
WHERE status = 'ACTIVE';

UPDATE matches
SET home_team = COALESCE(home_team, 'OK IIS'),
    away_team = COALESCE(away_team, 'Spartak');

UPDATE matches
SET away_team = 'Spartak'
WHERE away_team = 'Guest team';

INSERT INTO seats (row_label, seat_number, status, zone_id)
SELECT row_data.row_label, seat_data.seat_number, 'AVAILABLE', zone_data.zone_id
FROM (VALUES (1), (2), (3), (4)) AS zone_data(zone_id)
CROSS JOIN (VALUES ('A'), ('B'), ('C'), ('D')) AS row_data(row_label)
CROSS JOIN generate_series(1, 10) AS seat_data(seat_number);

SELECT setval(pg_get_serial_sequence('seats', 'id'), COALESCE((SELECT MAX(id) FROM seats), 1), true);

UPDATE seats
SET status = 'AVAILABLE'
WHERE status IN ('RESERVED', 'SOLD');

INSERT INTO promotions (id, name, discount_percentage, start_date, end_date, status)
VALUES
    (1, 'Student discount', 20.00, '2026-06-01', '2026-06-30', 'ACTIVE'),
    (2, 'Family offer', 10.00, '2026-06-10', '2026-07-10', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ticket_types (id, name, description, coefficient)
VALUES
    (1, 'REGULAR', 'Standard ticket', 1.00),
    (2, 'VIP', 'VIP ticket with better seating', 1.80)
ON CONFLICT (id) DO NOTHING;

DELETE FROM ticket_types
WHERE name IN ('STUDENT', 'CHILD', 'STUDENTSKA', 'DECIJA');

UPDATE users
SET role = CASE
    WHEN UPPER(role) IN ('CUSTOMER', 'KUPAC', 'ROLE_CUSTOMER', 'ROLE_KUPAC') THEN 'CUSTOMER'
    WHEN UPPER(role) IN ('MANAGER', 'MENADZER', 'ROLE_MANAGER', 'ROLE_MENADZER') THEN 'MANAGER'
    WHEN UPPER(role) IN ('ADMIN', 'ADMINISTRATOR', 'ROLE_ADMIN', 'ROLE_ADMINISTRATOR') THEN 'ADMIN'
    WHEN UPPER(role) IN ('STATISTICAR', 'ROLE_STATISTICAR') THEN 'STATISTICAR'
    WHEN UPPER(role) IN ('STRUCNI_STAB', 'STRUCNI STAB', 'ROLE_STRUCNI_STAB') THEN 'STRUCNI_STAB'
    ELSE 'CUSTOMER'
END
WHERE role IS NULL
   OR role NOT IN ('CUSTOMER', 'MANAGER', 'ADMIN', 'STATISTICAR', 'STRUCNI_STAB');

ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

ALTER TABLE users ADD CONSTRAINT users_role_check
CHECK (role IN ('CUSTOMER', 'MANAGER', 'ADMIN', 'STATISTICAR', 'STRUCNI_STAB'));

ALTER TABLE reservations DROP CONSTRAINT IF EXISTS reservations_status_check;

ALTER TABLE reservations ADD CONSTRAINT reservations_status_check
CHECK (status IN ('ACTIVE', 'CANCELLED', 'EXPIRED', 'SOLD'));

ALTER TABLE tickets DROP CONSTRAINT IF EXISTS tickets_status_check;

ALTER TABLE tickets ADD CONSTRAINT tickets_status_check
CHECK (status IN ('VALID', 'CANCELLED', 'REFUNDED'));

ALTER TABLE seats DROP CONSTRAINT IF EXISTS seats_status_check;

ALTER TABLE seats ADD CONSTRAINT seats_status_check
CHECK (status IN ('AVAILABLE', 'RESERVED', 'SOLD', 'BLOCKED'));
