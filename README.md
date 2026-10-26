# Шахматный клуб — КР №2

Настольное JavaFX-приложение для управления регистрациями игроков на
шахматные турниры. Интерфейс построен на JavaFX и FXML, данные хранятся в
PostgreSQL, доступ к базе выполняется напрямую через JDBC.

## Возможности

- вход по email и интерфейс с учётом ролей `ADMIN` и `PLAYER`;
- просмотр, поиск, фильтрация и сортировка заявок;
- создание заявки и изменение её статуса;
- сохранение истории через перевод заявки в `CANCELLED` вместо физического
  удаления;
- просмотр турниров, рейтинговых ограничений и свободных мест;
- проверка роли, статуса турнира, даты начала, рейтинга, свободных мест и
  повторной регистрации;
- статистика по пользователям, турнирам и статусам заявок;
- экспорт текущего отфильтрованного и отсортированного набора в `.xlsx`;
- PostgreSQL 17 в Docker Compose.

## Требования

- JDK 21;
- Maven 3.9 или новее;
- Docker Desktop либо Docker Engine с Docker Compose.

## Первый запуск после клонирования

```bash
git clone https://github.com/zhorikbla/chess-club.git
cd chess-club
git switch kr2-javafx
cp .env.example .env
cp src/main/resources/database.properties.example \
   src/main/resources/database.properties
docker compose --env-file .env up -d --wait
mvn clean javafx:run
```

Файлы `.env` и `database.properties` намеренно не отправляются в Git. Для
локального запуска они создаются копированием безопасных шаблонов.

Тестовый администратор:

```text
olga.smirnova@example.com
```

Тестовый игрок:

```text
ivan.petrov@example.com
```

## Обычный запуск

```bash
docker compose --env-file .env up -d --wait
mvn javafx:run
```

Проверка контейнера:

```bash
docker compose ps
```

Остановка без удаления данных:

```bash
docker compose down
```

Не используйте `docker compose down -v`, если данные требуется сохранить:
ключ `-v` удаляет именованный Docker volume.

## Структура

```text
docker-compose.yml                         PostgreSQL 17 в Docker
sql/                                       схема и тестовые данные
src/main/java/ru/mirea/project/fx/         запуск JavaFX и смена сцен
src/main/java/ru/mirea/project/controller/ обработчики FXML и интерфейса
src/main/java/ru/mirea/project/model/      сущности и enum
src/main/java/ru/mirea/project/service/    бизнес-правила
src/main/java/ru/mirea/project/repository/ JDBC и SQL
src/main/java/ru/mirea/project/database/   создание JDBC-соединений
src/main/java/ru/mirea/project/util/       экспорт Excel
src/main/resources/fxml/                   описание окон
src/main/resources/css/                    стили JavaFX
```

Консольная версия КР №1 сохранена в `Main.java` и запускается отдельно:

```bash
mvn compile exec:java
```
