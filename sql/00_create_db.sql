-- Выполнить один раз от имени суперпользователя PostgreSQL:
-- psql -h localhost -U postgres -f sql/00_create_db.sql

SET client_encoding = 'UTF8';

CREATE ROLE chess_app WITH LOGIN PASSWORD 'chess_app';
CREATE DATABASE chess_tournament OWNER chess_app;
