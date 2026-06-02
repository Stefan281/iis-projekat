ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;

INSERT INTO zones (id, name, description, price_coefficient, capacity, occupied_seats, occupancy_rate)
VALUES
    (1, 'VIP', 'Best seats near the court', 1.80, 60, 12, 20.00),
    (2, 'East', 'Central stand', 1.20, 250, 45, 18.00),
    (3, 'North', 'Standard stand', 1.00, 300, 30, 10.00)
ON CONFLICT (id) DO NOTHING;

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

INSERT INTO seats (id, row_label, seat_number, status, zone_id)
VALUES
    (1, 'A', 1, 'AVAILABLE', 1),
    (2, 'A', 2, 'BLOCKED', 1),
    (3, 'B', 15, 'AVAILABLE', 2),
    (4, 'C', 24, 'RESERVED', 3)
ON CONFLICT (id) DO NOTHING;

INSERT INTO promotions (id, name, discount_percentage, start_date, end_date, status)
VALUES
    (1, 'Student discount', 20.00, '2026-06-01', '2026-06-30', 'ACTIVE'),
    (2, 'Family offer', 10.00, '2026-06-10', '2026-07-10', 'ACTIVE')
ON CONFLICT (id) DO NOTHING;

INSERT INTO ticket_types (id, name, description, coefficient)
VALUES
    (1, 'REGULAR', 'Standard ticket', 1.00),
    (2, 'VIP', 'VIP ticket with better seating', 1.80),
    (3, 'STUDENT', 'Discounted student ticket', 0.80),
    (4, 'CHILD', 'Discounted child ticket', 0.60)
ON CONFLICT (id) DO NOTHING;
