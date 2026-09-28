# Шахматный клуб

Консольная информационная система на Java для управления пользователями,
турнирами и заявками игроков. Основная сущность проекта — регистрация игрока
на турнир. Данные хранятся в PostgreSQL, доступ выполняется через JDBC.

## Возможности MVP

- вход по email и разделение меню по ролям `ADMIN` и `PLAYER`;
- просмотр, создание, изменение и удаление пользователей;
- просмотр, создание, изменение и удаление турниров;
- просмотр всех заявок и изменение их статуса администратором;
- просмотр турниров, подача заявки, просмотр и отмена своих заявок игроком;
- проверки статуса турнира, даты начала и рейтинговых ограничений;
- защита от повторной активной заявки и удаления связанных данных.

## Требования

- JDK 17;
- Maven 3.9 или новее;
- PostgreSQL 16 или совместимая версия.

## Подготовка базы данных

Скрипт схемы удаляет существующие таблицы и данные. Для чистой установки:

```bash
psql -h localhost -U postgres -f sql/00_create_db.sql
psql -h localhost -U chess_app -d chess_tournament -f sql/01_schema.sql
psql -h localhost -U chess_app -d chess_tournament -f sql/02_data.sql
```

Пароль учебной роли PostgreSQL: `chess_app`.

## Запуск

```bash
mvn clean compile exec:java
```

Тестовый администратор:

```text
olga.smirnova@example.com
```

Тестовый игрок:

```text
alexey.morozov@example.com
```

## Структура

```text
sql/                                      создание БД, схема и тестовые данные
src/main/java/ru/mirea/project/Main.java  консольный интерфейс
src/main/java/ru/mirea/project/model/     предметные классы и enum
src/main/java/ru/mirea/project/repository JDBC и SQL запросы
src/main/java/ru/mirea/project/database/  создание соединений PostgreSQL
docs/                                     материалы по ТЗ и защите
```

Расширенный черновик хранится локально в `.reference/` и не попадает в Git.
