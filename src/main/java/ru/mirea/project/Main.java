package ru.mirea.project;

import ru.mirea.project.database.DatabaseManager;
import ru.mirea.project.model.Registration;
import ru.mirea.project.model.RegistrationStatus;
import ru.mirea.project.model.TimeControl;
import ru.mirea.project.model.Tournament;
import ru.mirea.project.model.TournamentStatus;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;
import ru.mirea.project.repository.RegistrationRepository;
import ru.mirea.project.repository.TournamentRepository;
import ru.mirea.project.repository.UserRepository;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://localhost:5432/chess_tournament";

        String databaseUser = "chess_app";
        String databasePassword = "chess_app";

        DatabaseManager databaseManager = new DatabaseManager(
            url,
            databaseUser,
            databasePassword
        );
        UserRepository userRepository =
                new UserRepository(databaseManager);
        TournamentRepository tournamentRepository =
                new TournamentRepository(databaseManager);
        RegistrationRepository registrationRepository =
                new RegistrationRepository(databaseManager);
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Введите email: ");
            String email = scanner.nextLine().trim();

            User currentUser = userRepository.findByEmail(email);

            if (currentUser == null) {
                System.out.println("Пользователь не найден");
                return;
            }

            System.out.println("Вход выполнен: ");
            System.out.println(currentUser);

            if (currentUser.getRole() == UserRole.ADMIN) {
                runAdminMenu(
                    scanner,
                    userRepository,
                    tournamentRepository,
                    registrationRepository,
                    currentUser
                );
            } else {
                runPlayerMenu(
                    scanner,
                    tournamentRepository,
                    registrationRepository,
                    currentUser
                );
            }
        } catch (SQLException exception) {
            System.out.println(
                "Ошибка работы с БД: "
                + exception.getMessage()
            );
        }
    }

    private static void runAdminMenu(
        Scanner scanner,
        UserRepository userRepository,
        TournamentRepository tournamentRepository,
        RegistrationRepository registrationRepository,
        User currentUser
    ) throws SQLException {
        boolean isRunning = true;

        while (isRunning) {
            System.out.println();
            System.out.println("=== МЕНЮ АДМИНИСТРАТОРА ===");
            System.out.println("1. Показать всех пользователей");
            System.out.println("2. Показать турниры");
            System.out.println("3. Показать заявки");
            System.out.println("4. Создать пользователя");
            System.out.println("5. Изменить пользователя");
            System.out.println("6. Удалить пользователя");
            System.out.println("7. Создать турнир");
            System.out.println("8. Изменить турнир");
            System.out.println("9. Удалить турнир");
            System.out.println("10. Изменить статус заявки");
            System.out.println("0. Выход");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> {
                    List<User> users = userRepository.findAll();
                    System.out.println();
                    System.out.println("=== ПОЛЬЗОВАТЕЛИ ===");
                    for (User user : users) {
                        System.out.println(user);
                    }
                }
                case "2" -> {
                    List<Tournament> tournaments =
                        tournamentRepository.findAll();

                    System.out.println();
                    System.out.println("=== ТУРНИРЫ ===");

                    for (Tournament tournament : tournaments) {
                        System.out.println(tournament);
                    }
                }
                case "3" -> {
                    List<Registration> registrations =
                        registrationRepository.findAll();

                    System.out.println();
                    System.out.println("=== ЗАЯВКИ ===");

                    for (Registration registration : registrations) {
                        System.out.println(registration);
                    }
                }
                case "4" ->
                    createUser(scanner, userRepository);
                case "5" ->
                    updateUser(scanner, userRepository);
                case "6" ->
                    deleteUser(scanner, userRepository, currentUser);
                case "7" ->
                    createTournament(scanner, tournamentRepository);
                case "8" ->
                    updateTournament(scanner, tournamentRepository);
                case "9" ->
                    deleteTournament(scanner, tournamentRepository);
                case "10" ->
                    changeRegistrationStatus(scanner, registrationRepository);
                case "0" -> {
                    isRunning = false;
                    System.out.println("Выход из программы");
                }
                default -> System.out.println("Неизвестный ввод");
            }
        }
    }
    private static void createUser(
        Scanner scanner,
        UserRepository userRepository
    ) {
        try {
            System.out.print("ФИО: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Роль: ");
            UserRole role = UserRole.valueOf(
                scanner.nextLine().trim().toUpperCase()
            );

            System.out.print("Рейтинг: ");
            int rating = Integer.parseInt(
                scanner.nextLine().trim()
            );
            User createdUser = userRepository.create(
                fullName,
                email,
                role,
                rating
            );

            System.out.println("Пользователь создан: ");
            System.out.println(createdUser);
        } catch (NumberFormatException exception) {
            System.out.println(
                "Ошибка: рейтинг должен быть целым числом"
            );
        } catch (IllegalArgumentException exception) {
            System.out.println(
                "Ошибка: неверная роль или некорректные данные"
            );
        } catch (SQLException exception) {
            System.out.println(
                "Не удалось создать пользователя: "
                + exception.getMessage()
            );
        }
    }

    private static void updateUser(
        Scanner scanner,
        UserRepository userRepository
    ) {
        try {
            System.out.print("ID пользователя: ");
            long id = Long.parseLong(
                scanner.nextLine().trim()
            );

            System.out.print("Новое ФИО: ");
            String fullName = scanner.nextLine().trim();

            System.out.print("Новый Email: ");
            String email = scanner.nextLine().trim();

            System.out.print("Новая роль: ");
            UserRole role = UserRole.valueOf(
                scanner.nextLine().trim().toUpperCase()
            );

            System.out.print("Новый рейтинг: ");
            int rating = Integer.parseInt(
                scanner.nextLine().trim()
            );
            boolean updated = userRepository.update(
                id,
                fullName,
                email,
                role,
                rating
            );

            if (updated) {
                System.out.println("Пользователь изменён");
            } else {
                System.out.println("Пользователь с ID " + id + " не найден");
            }
        } catch (NumberFormatException exception) {
            System.out.println(
                "Ошибка: рейтинг и ID должны быть целыми числами"
            );
        } catch (IllegalArgumentException exception) {
            System.out.println(
                "Ошибка: неверная роль или некорректные данные"
            );
        } catch (SQLException exception) {
            System.out.println(
                "Не удалось изменить пользователя: "
                + exception.getMessage()
            );
        }
    }

    private static void deleteUser(
        Scanner scanner,
        UserRepository userRepository,
        User currentUser
    ) {
        try {
            System.out.print("ID пользователя для удаления: ");

            long id = Long.parseLong(
                scanner.nextLine().trim()
            );

            if (id == currentUser.getId()) {
                System.out.println(
                    "Нельзя удалить пользователя, под которым выполнен активный вход"
                );
                return;
            }
            System.out.print("Для подтверждения напишите Y/Да: ");
            String confirmation = scanner.nextLine().trim();
            if (!confirmation.equalsIgnoreCase("да")
                && !confirmation.equalsIgnoreCase("y")) {
                System.out.println("Удаление отменено");
                return;
            }

            boolean deleted = userRepository.deleteById(id);

            if (deleted) {
                System.out.println("Пользователь удалён");
            } else {
                System.out.println("Пользователь с ID " + id + " не найден");
            }
        } catch (NumberFormatException exception) {
            System.out.println("Ошибка: ID должен быть целым числом");
        } catch (SQLException exception) {
            if ("23503".equals(exception.getSQLState())) {
                System.out.println(
                    "Нельзя удалить пользователя: у него существуют заявки"
                );
            } else {
                System.out.println(
                    "Не удалось удалить пользователя: "
                    + exception.getMessage()
                );
            }
        }
    }

    private static void createTournament(
        Scanner scanner,
        TournamentRepository tournamentRepository
    ) {
        try {
            System.out.print("Название турнира: ");
            String title = scanner.nextLine().trim();

            System.out.print("Контроль времени (BULLET, BLITZ, RAPID, CLASSICAL): ");
            TimeControl timeControl = TimeControl.valueOf(
                scanner.nextLine().trim().toUpperCase()
            );

            System.out.print("Дата начала (ГГГГ-ММ-ДД): ");
            LocalDate startDate = LocalDate.parse(
                scanner.nextLine().trim()
            );
            System.out.print("Дата окончания (ГГГГ-ММ-ДД): ");
            LocalDate endDate = LocalDate.parse(
                scanner.nextLine().trim()
            );
            System.out.print("Максимальное количество участников: ");
            int maxParticipants = Integer.parseInt(
                scanner.nextLine().trim()
            );
            System.out.print("Минимальный рейтинг (Enter - без ограничений): ");
            Integer minRating = readNullableInteger(scanner);
            System.out.print("Максимальный рейтинг (Enter - без ограничений): ");
            Integer maxRating = readNullableInteger(scanner);
            Tournament tournament = tournamentRepository.create(
                title,
                timeControl,
                startDate,
                endDate,
                maxParticipants,
                minRating,
                maxRating
            );
            System.out.println("Турнир создан:");
            System.out.println(tournament);
        } catch (NumberFormatException exception) {
            System.out.println(
                "Ошибка: количество участников "
                + "и рейтинги должны быть целыми числами"
            );
        } catch (DateTimeParseException exception) {
            System.out.println(
                "Ошибка: дата должна иметь формат ГГГГ-ММ-ДД"
            );
        } catch (IllegalArgumentException exception) {
            System.out.println(
                "Ошибка: неверный контроль времени "
                + "или некорректные данные"
            );
        } catch (SQLException exception) {
            System.out.println(
                "Не удалось создать турнир: "
                + exception.getMessage()
            );
        }
    }
    private static Integer readNullableInteger(
        Scanner scanner
    ) {
        String input = scanner.nextLine().trim();

        if (input.isEmpty()) {
            return null;
        }

        return Integer.parseInt(input);
    }
    private static void updateTournament(
        Scanner scanner,
        TournamentRepository tournamentRepository
    ) {
        try {
            System.out.print("ID турнира: ");
            long id = Long.parseLong(
                scanner.nextLine().trim()
            );
            System.out.print("Новое название: ");
            String title = scanner.nextLine().trim();

            System.out.print(
                "Контроль времени "
                + "(BULLET, BLITZ, RAPID, CLASSICAL): "
            );
            TimeControl timeControl = TimeControl.valueOf(
                scanner.nextLine().trim().toUpperCase()
            );

            System.out.print("Дата начала (ГГГГ-ММ-ДД): ");
            LocalDate startDate = LocalDate.parse(
                scanner.nextLine().trim()
            );

            System.out.print("Дата окончания (ГГГГ-ММ-ДД): ");
            LocalDate endDate = LocalDate.parse(
                scanner.nextLine().trim()
            );

            System.out.print("Максимум участников: ");
            int maxParticipants = Integer.parseInt(
                scanner.nextLine().trim()
            );
            System.out.print(
                "Минимальный рейтинг "
                + "(Enter — без ограничения): "
            );
            Integer minRating = readNullableInteger(scanner);

            System.out.print(
                "Максимальный рейтинг "
                + "(Enter — без ограничения): "
            );
            Integer maxRating = readNullableInteger(scanner);

            System.out.print(
                "Статус "
                + "(REGISTRATION_OPEN, REGISTRATION_CLOSED, "
                + "IN_PROGRESS, FINISHED, CANCELLED): "
            );
            TournamentStatus status = TournamentStatus.valueOf(
                scanner.nextLine().trim().toUpperCase()
            );
            boolean updated = tournamentRepository.update(
                id,
                title,
                timeControl,
                startDate,
                endDate,
                maxParticipants,
                minRating,
                maxRating,
                status
            );
            if (updated) {
                System.out.println("Турнир изменён");
            } else {
                System.out.println("Турнир с ID " + id + " не найден");
            }
        } catch (NumberFormatException exception) {
            System.out.println(
                "Ошибка: ID, места и рейтинги должны быть числами"
            );
        } catch (DateTimeParseException exception) {
            System.out.println(
                "Ошибка: дата должна иметь формат ГГГГ-ММ-ДД"
            );
        } catch (IllegalArgumentException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
        } catch (SQLException exception) {
            System.out.println(
                "Не удалось изменить турнир: "
                + exception.getMessage()
            );
        }
    }
    private static void deleteTournament(
        Scanner scanner,
        TournamentRepository tournamentRepository
    ) {
        try {
            System.out.print("ID турнира для удаления: ");

            long id = Long.parseLong(
                scanner.nextLine().trim()
            );
            System.out.print("Для подтверждения напишите Y/Да: ");
            String confirmation = scanner.nextLine().trim();
            boolean confirmed = confirmation.equalsIgnoreCase("да")
                || confirmation.equalsIgnoreCase("y");

            if (!confirmed) {
                System.out.println("Удаление отменено");
                return;
            }
            boolean deleted = tournamentRepository.deleteById(id);

            if (deleted) {
                System.out.println("Турнир удалён");
            } else {
                System.out.println(
                    "Турнир с ID " + id + " не найден"
                );
            }
        } catch (NumberFormatException exception) {
            System.out.println(
                "Ошибка: ID должен быть целым числом"
            );
        } catch (SQLException exception) {
            if ("23503".equals(exception.getSQLState())) {
                System.out.println(
                    "Нельзя удалить турнир: "
                    + "на него существуют заявки"
                );
            } else {
                System.out.println(
                    "Не удалось удалить турнир: "
                    + exception.getMessage()
                );
            }
        }
    }
    private static void runPlayerMenu(
        Scanner scanner,
        TournamentRepository tournamentRepository,
        RegistrationRepository registrationRepository,
        User currentUser
    ) throws SQLException {

        boolean isRunning = true;

        while (isRunning) {
            System.out.println();
            System.out.println("=== МЕНЮ ИГРОКА ===");
            System.out.println("1. Показать турниры");
            System.out.println("2. Подать заявку");
            System.out.println("3. Мои заявки");
            System.out.println("4. Отменить заявку");
            System.out.println("0. Выход");
            System.out.print("Выберите действие: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    List<Tournament> tournaments =
                        tournamentRepository.findAll();

                    System.out.println();
                    System.out.println("=== ТУРНИРЫ ===");

                    for (Tournament tournament : tournaments) {
                        System.out.println(tournament);
                    }
                }

                case "2" ->
                    createRegistration(
                        scanner,
                        tournamentRepository,
                        registrationRepository,
                        currentUser
                    );
                case "3" -> {
                    List<Registration> registrations =
                        registrationRepository.findByUserId(
                            currentUser.getId()
                        );

                    System.out.println();
                    System.out.println("=== МОИ ЗАЯВКИ ===");

                    if (registrations.isEmpty()) {
                        System.out.println("Заявок пока нет");
                    } else {
                        for (Registration registration : registrations) {
                            System.out.println(registration);
                        }
                    }
                }

                case "4" ->
                    cancelRegistration(
                        scanner,
                        registrationRepository,
                        currentUser
                    );
                case "0" -> {
                    isRunning = false;
                    System.out.println("Выход из программы");
                }

                default ->
                    System.out.println("Неизвестный пункт меню");
            }
        }
    }
    private static void createRegistration(
        Scanner scanner,
        TournamentRepository tournamentRepository,
        RegistrationRepository registrationRepository,
        User currentUser
    ) {
        try {
            System.out.print("Введите ID турнира: ");

            long tournamentId = Long.parseLong(
                scanner.nextLine().trim()
            );

            Tournament tournament =
                tournamentRepository.findById(tournamentId);

            if (tournament == null) {
                System.out.println(
                    "Турнир с ID "
                    + tournamentId
                    + " не найден"
                );
                return;
            }

            if (tournament.getStatus()
                != TournamentStatus.REGISTRATION_OPEN) {
                System.out.println(
                    "Регистрация на этот турнир закрыта"
                );
                return;
            }

            if (!tournament.getStartDate().isAfter(LocalDate.now())) {
                System.out.println(
                    "Нельзя зарегистрироваться "
                    + "на начавшийся турнир"
                );
                return;
            }

            Integer minRating = tournament.getMinRating();

            if (minRating != null && currentUser.getRating() < minRating) {

                System.out.println(
                    "Недостаточный рейтинг. Минимум: "
                    + minRating
                );
                return;
            }

            Integer maxRating = tournament.getMaxRating();

            if (maxRating != null && currentUser.getRating() > maxRating) {

                System.out.println(
                    "Рейтинг превышает допустимый максимум: "
                    + maxRating
                );
                return;
            }

            long registrationId = registrationRepository.create(
                currentUser.getId(),
                tournamentId
            );

            System.out.println(
                "Заявка #" + registrationId
                + " успешно создана"
            );

        } catch (NumberFormatException exception) {
            System.out.println(
                "Ошибка: ID должен быть целым числом"
            );
        } catch (SQLException exception) {
            if ("23505".equals(exception.getSQLState())) {
                System.out.println(
                    "У вас уже есть активная заявка "
                    + "на этот турнир"
                );
            } else {
                System.out.println(
                    "Не удалось создать заявку: "
                    + exception.getMessage()
                );
            }
        }
    }
    private static void cancelRegistration(
        Scanner scanner,
        RegistrationRepository registrationRepository,
        User currentUser
    ) {
        try {
            System.out.print(
                "Введите ID заявки для отмены: "
            );

            long registrationId = Long.parseLong(
                scanner.nextLine().trim()
            );

            boolean cancelled = registrationRepository.cancelByUser(
                registrationId,
                currentUser.getId()
            );

            if (cancelled) {
                System.out.println("Заявка отменена");
            } else {
                System.out.println(
                    "Заявка не найдена, принадлежит другому игроку "
                    + "или уже завершена"
                );
            }

        } catch (NumberFormatException exception) {
            System.out.println(
                "Ошибка: ID должен быть целым числом"
            );
        } catch (SQLException exception) {
            System.out.println(
                "Не удалось отменить заявку: "
                + exception.getMessage()
            );
        }
    }
    private static void changeRegistrationStatus(
        Scanner scanner,
        RegistrationRepository registrationRepository
    ) {
        try {
            System.out.print("Введите ID заявки: ");

            long registrationId = Long.parseLong(
               scanner.nextLine().trim()
            );

            Registration registration =
                registrationRepository.findById(registrationId);

            if (registration == null) {
                System.out.println(
                    "Заявка с ID "
                    + registrationId
                    + " не найдена"
                );
                return;
            }

            System.out.print(
                "Новый статус "
                + "(PENDING, CONFIRMED, REJECTED, CANCELLED): "
            );

            RegistrationStatus newStatus = RegistrationStatus.valueOf(
                scanner.nextLine().trim().toUpperCase()
            );

            registration.changeStatus(newStatus);

            boolean updated = registrationRepository.updateStatus(
                registrationId,
                newStatus
            );

            if (updated) {
                System.out.println(
                    "Статус заявки изменён на "
                    + newStatus
                );
            } else {
                System.out.println("Заявка не найдена");
            }

        } catch (NumberFormatException exception) {
            System.out.println(
                "Ошибка: ID должен быть целым числом"
            );
        } catch (IllegalArgumentException exception) {
            System.out.println(
                "Ошибка: неизвестный статус"
            );
        } catch (IllegalStateException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
        } catch (SQLException exception) {
            System.out.println(
                "Не удалось изменить заявку: "
                + exception.getMessage()
            );
        }
    }
}
