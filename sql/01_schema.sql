-- Внимание: скрипт пересоздаёт таблицы и удаляет находящиеся в них данные.
-- psql -h localhost -U chess_app -d chess_tournament -f sql/01_schema.sql

SET client_encoding = 'UTF8';
SET client_min_messages = warning;

DROP TABLE IF EXISTS registrations;
DROP TABLE IF EXISTS tournaments;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id         BIGINT       GENERATED ALWAYS AS IDENTITY,
    full_name  VARCHAR(100) NOT NULL,
    email      VARCHAR(255) NOT NULL,
    role       VARCHAR(20)  NOT NULL DEFAULT 'PLAYER',
    rating     INTEGER      NOT NULL DEFAULT 1200,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT users_pk                  PRIMARY KEY (id),
    CONSTRAINT users_email_unique        UNIQUE (email),
    CONSTRAINT users_full_name_not_blank CHECK (btrim(full_name) <> ''),
    CONSTRAINT users_email_format        CHECK (email ~ '^[^@\s]+@[^@\s]+\.[^@\s]+$'),
    CONSTRAINT users_email_lowercase     CHECK (email = lower(email)),
    CONSTRAINT users_role_valid          CHECK (role IN ('PLAYER', 'ADMIN')),
    CONSTRAINT users_rating_range        CHECK (rating BETWEEN 0 AND 3500)
);

CREATE TABLE tournaments (
    id               BIGINT       GENERATED ALWAYS AS IDENTITY,
    title            VARCHAR(150) NOT NULL,
    time_control     VARCHAR(20)  NOT NULL,
    start_date       DATE         NOT NULL,
    end_date         DATE         NOT NULL,
    max_participants INTEGER      NOT NULL,
    min_rating       INTEGER,
    max_rating       INTEGER,
    status           VARCHAR(30)  NOT NULL DEFAULT 'REGISTRATION_OPEN',

    CONSTRAINT tournaments_pk                 PRIMARY KEY (id),
    CONSTRAINT tournaments_title_date_unique  UNIQUE (title, start_date),
    CONSTRAINT tournaments_title_not_blank    CHECK (btrim(title) <> ''),
    CONSTRAINT tournaments_time_control_valid CHECK (time_control IN
        ('BULLET', 'BLITZ', 'RAPID', 'CLASSICAL')),
    CONSTRAINT tournaments_status_valid       CHECK (status IN
        ('REGISTRATION_OPEN', 'REGISTRATION_CLOSED', 'IN_PROGRESS', 'FINISHED', 'CANCELLED')),
    CONSTRAINT tournaments_dates_order        CHECK (end_date >= start_date),
    CONSTRAINT tournaments_capacity_range     CHECK (max_participants BETWEEN 2 AND 500),
    CONSTRAINT tournaments_rating_bounds      CHECK (
        (min_rating IS NULL OR min_rating BETWEEN 0 AND 3500)
        AND (max_rating IS NULL OR max_rating BETWEEN 0 AND 3500)
        AND (min_rating IS NULL OR max_rating IS NULL OR min_rating <= max_rating))
);

CREATE TABLE registrations (
    id            BIGINT      GENERATED ALWAYS AS IDENTITY,
    user_id       BIGINT      NOT NULL,
    tournament_id BIGINT      NOT NULL,
    status        VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    registered_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT registrations_pk            PRIMARY KEY (id),
    CONSTRAINT registrations_user_fk       FOREIGN KEY (user_id)
        REFERENCES users (id) ON DELETE RESTRICT,
    CONSTRAINT registrations_tournament_fk FOREIGN KEY (tournament_id)
        REFERENCES tournaments (id) ON DELETE RESTRICT,
    CONSTRAINT registrations_status_valid  CHECK (status IN
        ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELLED'))
);

-- Игрок может иметь только одну активную заявку на один турнир.
CREATE UNIQUE INDEX registrations_one_active_per_player
    ON registrations (user_id, tournament_id)
    WHERE status IN ('PENDING', 'CONFIRMED');

CREATE INDEX registrations_tournament_idx
    ON registrations (tournament_id);
