-- Тестовые данные. Выполнять после 01_schema.sql:
-- psql -h localhost -U chess_app -d chess_tournament -f sql/02_data.sql

SET client_encoding = 'UTF8';

INSERT INTO users (full_name, email, role, rating) VALUES
    ('Ольга Смирнова',     'olga.smirnova@example.com',      'ADMIN',  1200),
    ('Сергей Орлов',       'sergey.orlov@example.com',       'ADMIN',  1200),
    ('Иван Петров',        'ivan.petrov@example.com',        'PLAYER', 1450),
    ('Анна Кузнецова',     'anna.kuznetsova@example.com',    'PLAYER', 1720),
    ('Дмитрий Соколов',    'dmitry.sokolov@example.com',     'PLAYER', 1980),
    ('Мария Волкова',      'maria.volkova@example.com',      'PLAYER', 2150),
    ('Алексей Морозов',    'alexey.morozov@example.com',     'PLAYER', 1310),
    ('Екатерина Лебедева', 'ekaterina.lebedeva@example.com', 'PLAYER', 1860),
    ('Никита Павлов',      'nikita.pavlov@example.com',      'PLAYER', 2310),
    ('Полина Новикова',    'polina.novikova@example.com',    'PLAYER', 1550);

INSERT INTO tournaments (
    title, time_control, start_date, end_date,
    max_participants, min_rating, max_rating, status
) VALUES
    ('Осенний блиц',        'BLITZ',     CURRENT_DATE + 14, CURRENT_DATE + 14, 16, NULL, NULL, 'REGISTRATION_OPEN'),
    ('Кубок новичков',      'RAPID',     CURRENT_DATE + 21, CURRENT_DATE + 22,  4, NULL, 1600, 'REGISTRATION_OPEN'),
    ('Открытый рапид',      'RAPID',     CURRENT_DATE + 30, CURRENT_DATE + 31, 32, 1400, NULL, 'REGISTRATION_OPEN'),
    ('Мастерский турнир',   'CLASSICAL', CURRENT_DATE + 45, CURRENT_DATE + 52, 10, 2000, NULL, 'REGISTRATION_OPEN'),
    ('Ночная пуля',         'BULLET',    CURRENT_DATE + 7,  CURRENT_DATE + 7,   2, NULL, NULL, 'REGISTRATION_OPEN'),
    ('Зимний рапид',        'RAPID',     CURRENT_DATE + 90, CURRENT_DATE + 91, 24, NULL, NULL, 'REGISTRATION_CLOSED'),
    ('Летний классический', 'CLASSICAL', CURRENT_DATE - 60, CURRENT_DATE - 54, 20, NULL, NULL, 'FINISHED');

INSERT INTO registrations (user_id, tournament_id, status, registered_at)
SELECT u.id, t.id, v.status, v.registered_at
FROM (VALUES
    ('ivan.petrov@example.com',        'Осенний блиц',        'CONFIRMED', LOCALTIMESTAMP - INTERVAL '12 days'),
    ('anna.kuznetsova@example.com',    'Осенний блиц',        'CONFIRMED', LOCALTIMESTAMP - INTERVAL '11 days'),
    ('dmitry.sokolov@example.com',     'Осенний блиц',        'PENDING',   LOCALTIMESTAMP - INTERVAL '2 days'),
    ('polina.novikova@example.com',    'Осенний блиц',        'PENDING',   LOCALTIMESTAMP - INTERVAL '1 day'),
    ('ekaterina.lebedeva@example.com', 'Осенний блиц',        'CANCELLED', LOCALTIMESTAMP - INTERVAL '9 days'),
    ('ivan.petrov@example.com',        'Кубок новичков',      'CONFIRMED', LOCALTIMESTAMP - INTERVAL '8 days'),
    ('alexey.morozov@example.com',     'Кубок новичков',      'PENDING',   LOCALTIMESTAMP - INTERVAL '3 days'),
    ('polina.novikova@example.com',    'Кубок новичков',      'PENDING',   LOCALTIMESTAMP - INTERVAL '1 day'),
    ('dmitry.sokolov@example.com',     'Открытый рапид',      'CONFIRMED', LOCALTIMESTAMP - INTERVAL '7 days'),
    ('maria.volkova@example.com',      'Открытый рапид',      'PENDING',   LOCALTIMESTAMP - INTERVAL '4 days'),
    ('anna.kuznetsova@example.com',    'Открытый рапид',      'REJECTED',  LOCALTIMESTAMP - INTERVAL '6 days'),
    ('maria.volkova@example.com',      'Мастерский турнир',   'CONFIRMED', LOCALTIMESTAMP - INTERVAL '5 days'),
    ('nikita.pavlov@example.com',      'Мастерский турнир',   'CONFIRMED', LOCALTIMESTAMP - INTERVAL '5 days'),
    ('ekaterina.lebedeva@example.com', 'Ночная пуля',         'CONFIRMED', LOCALTIMESTAMP - INTERVAL '3 days'),
    ('nikita.pavlov@example.com',      'Ночная пуля',         'PENDING',   LOCALTIMESTAMP - INTERVAL '2 days'),
    ('dmitry.sokolov@example.com',     'Летний классический', 'CONFIRMED', LOCALTIMESTAMP - INTERVAL '75 days'),
    ('maria.volkova@example.com',      'Летний классический', 'CONFIRMED', LOCALTIMESTAMP - INTERVAL '74 days'),
    ('nikita.pavlov@example.com',      'Летний классический', 'CANCELLED', LOCALTIMESTAMP - INTERVAL '70 days')
) AS v (email, title, status, registered_at)
JOIN users u ON u.email = v.email
JOIN tournaments t ON t.title = v.title;
