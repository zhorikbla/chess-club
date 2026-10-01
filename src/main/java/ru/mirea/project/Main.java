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
import ru.mirea.project.util.ExcelExporter;
import ru.mirea.project.model.RegistrationStatistics;
import ru.mirea.project.exception.BusinessException;
import ru.mirea.project.service.RegistrationService;
import ru.mirea.project.service.TournamentService;
import ru.mirea.project.service.UserService;
import ru.mirea.project.service.StatisticsService;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Comparator;

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
        UserService userService =
                new UserService(userRepository);
        TournamentRepository tournamentRepository =
                new TournamentRepository(databaseManager);
        TournamentService tournamentService =
                new TournamentService(tournamentRepository);
        RegistrationRepository registrationRepository =
                new RegistrationRepository(databaseManager);
        RegistrationService registrationService =
                new RegistrationService(
                    registrationRepository,
                    userRepository,
                    tournamentRepository
                );
        StatisticsService statisticsService =
                new StatisticsService(
                    userRepository,
                    tournamentRepository,
                    registrationRepository
                );
        ExcelExporter excelExporter = new ExcelExporter();
        try (Scanner scanner = new Scanner(System.in)) {
            System.out.print("Введите email: ");
            String email = scanner.nextLine().trim();

        User currentUser = userService.findByEmail(email);
            if (currentUser == null) {
                System.out.println("Пользователь не найден");
                return;
            }

            System.out.println("Вход выполнен: ");
            System.out.println(currentUser);

            if (currentUser.getRole() == UserRole.ADMIN) {
                runAdminMenu(
                    scanner,
                    userService,
                    tournamentService,
                    registrationService,
                    excelExporter,
                    statisticsService,
                    currentUser
                );
            } else {
                runPlayerMenu(
                    scanner,
                    tournamentService,
                    registrationService,
                    currentUser
                );
            }
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
        } catch (SQLException exception) {
            System.out.println(
                "Ошибка работы с БД: "
                + exception.getMessage()
            );
        }
    }

    private static void runAdminMenu(
        Scanner scanner,
        UserService userService,
        TournamentService tournamentService,
        RegistrationService registrationService,
        ExcelExporter excelExporter,
        StatisticsService statisticsService,
        User currentUser
    ) throws SQLException {
        boolean isRunning = true;

        while (isRunning) {
            System.out.println();
        System.out.println("=== МЕНЮ АДМИНИСТРАТОРА ===");
        System.out.println("1. Управление пользователями");
        System.out.println("2. Управление турнирами");
        System.out.println("3. Управление заявками");
        System.out.println("4. Экспорт данных в Excel");
        System.out.println("5. Показать статистику");
        System.out.println("0. Выход");
        System.out.print("Выберите действие: ");

            String choice = scanner.nextLine().trim();
            switch (choice) {
                case "1" -> runUsersMenu(
                scanner,
                userService,
                currentUser
            );
            case "2" -> runTournamentsMenu(
                scanner,
                tournamentService
            );
            case "3" -> runRegistrationsMenu(
                scanner,
                registrationService
            );
            case "4" ->
                exportToExcel(
                    userService,
                    tournamentService,
                    registrationService,
                    excelExporter
                );
            case "5" ->
                showStatistics(statisticsService);
            case "0" -> {
                isRunning = false;
                System.out.println("Выход из программы");
            }
            default ->
                System.out.println("Неизвестный пункт меню");
            }
        }
    }
    private static void runUsersMenu(
    Scanner scanner,
    UserService userService,
    User currentUser
) throws SQLException {
    boolean isRunning = true;

    while (isRunning) {
        System.out.println();
        System.out.println("=== УПРАВЛЕНИЕ ПОЛЬЗОВАТЕЛЯМИ ===");
        System.out.println("1. Показать всех пользователей");
        System.out.println("2. Создать пользователя");
        System.out.println("3. Изменить пользователя");
        System.out.println("4. Удалить пользователя");
        System.out.println("0. Назад");
        System.out.print("Выберите действие: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                List<User> users = userService.findAll();
                System.out.println();
                System.out.println("=== ПОЛЬЗОВАТЕЛИ ===");

                for (User user : users) {
                    System.out.println(user);
                }
            }
            case "2" ->
                createUser(scanner, userService);
            case "3" ->
                updateUser(scanner, userService);
            case "4" ->
                deleteUser(
                    scanner,
                    userService,
                    currentUser
                );
            case "0" ->
                isRunning = false;
            default ->
                System.out.println("Неизвестный пункт меню");
        }
    }
}
private static void runTournamentsMenu(
    Scanner scanner,
    TournamentService tournamentService
) throws SQLException {
    boolean isRunning = true;

    while (isRunning) {
        System.out.println();
        System.out.println("=== УПРАВЛЕНИЕ ТУРНИРАМИ ===");
        System.out.println("1. Показать турниры");
        System.out.println("2. Создать турнир");
        System.out.println("3. Изменить турнир");
        System.out.println("4. Удалить турнир");
        System.out.println("0. Назад");
        System.out.print("Выберите действие: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> {
                List<Tournament> tournaments =
                    tournamentService.findAll();

                System.out.println();
                System.out.println("=== ТУРНИРЫ ===");

                for (Tournament tournament : tournaments) {
                    System.out.println(tournament);
                }
            }
            case "2" ->
                createTournament(
                    scanner,
                    tournamentService
                );
            case "3" ->
                updateTournament(
                    scanner,
                    tournamentService
                );
            case "4" ->
                deleteTournament(
                    scanner,
                    tournamentService
                );
            case "0" ->
                isRunning = false;
            default ->
                System.out.println("Неизвестный пункт меню");
        }
    }
}
private static void runRegistrationsMenu(
    Scanner scanner,
    RegistrationService registrationService
) throws SQLException {
    boolean isRunning = true;

    while (isRunning) {
        System.out.println();
        System.out.println("=== УПРАВЛЕНИЕ ЗАЯВКАМИ ===");
        System.out.println("1. Показать все заявки");
        System.out.println("2. Найти заявку по ID");
        System.out.println("3. Изменить статус заявки");
        System.out.println("4. Поиск по игроку");
        System.out.println("5. Поиск по турниру");
        System.out.println("6. Фильтр по статусу");
        System.out.println("7. Фильтр по диапазону дат");
        System.out.println("8. Сортировка по дате");
        System.out.println("9. Сортировка по рейтингу");
        System.out.println("0. Назад");
        System.out.print("Выберите действие: ");

        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" ->
                printRegistrations(
                    "ВСЕ ЗАЯВКИ",
                    registrationService.findAll()
                );
            case "2" ->
                findRegistrationById(
                    scanner,
                    registrationService
                );
            case "3" ->
                changeRegistrationStatus(
                    scanner,
                    registrationService
                );
            case "4" ->
                searchRegistrationsByPlayer(
                    scanner,
                    registrationService
                );
            case "5" ->
                searchRegistrationsByTournament(
                    scanner,
                    registrationService
                );
            case "6" ->
                filterRegistrationsByStatus(
                    scanner,
                    registrationService
                );
            case "7" ->
                filterRegistrationsByDate(
                    scanner,
                    registrationService
                );
            case "8" ->
                printRegistrations(
                    "ЗАЯВКИ ПО ДАТЕ",
                    registrationService.sortByRegisteredAt()
                );
            case "9" ->
                printRegistrations(
                    "ЗАЯВКИ ПО РЕЙТИНГУ ИГРОКА",
                    registrationService
                        .sortByPlayerRatingDescending()
                );
            case "0" ->
                isRunning = false;
            default ->
                System.out.println("Неизвестный пункт меню");
        }
    }
}
    private static void createUser(
        Scanner scanner,
        UserService userService
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
            User createdUser = userService.create(
                fullName,
                email,
                role,
                rating
            );

            System.out.println("Пользователь создан: ");
            System.out.println(createdUser);
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
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
        UserService userService
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
            userService.update(
                id,
                fullName,
                email,
                role,
                rating
            );

            System.out.println("Пользователь изменён");
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
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
        UserService userService,
        User currentUser
    ) {
        try {
            System.out.print("ID пользователя для удаления: ");

            long id = Long.parseLong(
                scanner.nextLine().trim()
            );

            System.out.print("Для подтверждения напишите Y/Да: ");
            String confirmation = scanner.nextLine().trim();
            if (!confirmation.equalsIgnoreCase("да")
                && !confirmation.equalsIgnoreCase("y")) {
                System.out.println("Удаление отменено");
                return;
            }

            userService.delete(
                id,
                currentUser.getId()
            );

            System.out.println("Пользователь удалён");
        } catch (NumberFormatException exception) {
            System.out.println("Ошибка: ID должен быть целым числом");
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
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
    private static void exportToExcel(
        UserService userService,
        TournamentService tournamentService,
        RegistrationService registrationService,
        ExcelExporter excelExporter
    ) {
        try {
            Path outputFile = excelExporter.export(
                Path.of("exports"),
                userService.findAll(),
                tournamentService.findAll()
                    .stream()
                    .sorted(
                        Comparator.comparingLong(Tournament::getId)
                    )
                    .toList(),
                registrationService.findAll()
            );

            System.out.println("Excel успешно создан: "+outputFile);

        } catch (SQLException exception){
            System.out.println(
                "Не удалось получить данные из БД:" +exception.getMessage()
            );
        } catch (IOException exception){
            System.out.println("Не удалос создать excel: "+exception.getMessage());
        }
    }

    private static void createTournament(
        Scanner scanner,
        TournamentService tournamentService
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
            Tournament tournament = tournamentService.create(
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
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
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
        TournamentService tournamentService
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
            tournamentService.update(
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
            System.out.println("Турнир изменён");
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
        } catch (BusinessException exception) {
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
        TournamentService tournamentService
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
            tournamentService.delete(id);
            System.out.println("Турнир удалён");
        } catch (NumberFormatException exception) {
            System.out.println(
                "Ошибка: ID должен быть целым числом"
            );
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
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
        TournamentService tournamentService,
        RegistrationService registrationService,
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
                        tournamentService.findAll();

                    System.out.println();
                    System.out.println("=== ТУРНИРЫ ===");

                    for (Tournament tournament : tournaments) {
                        System.out.println(tournament);
                    }
                }

                case "2" ->
                    createRegistration(
                        scanner,
                        registrationService,
                        currentUser
                    );
                case "3" -> {
                    List<Registration> registrations =
                        registrationService.findByUserId(
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
                        registrationService,
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
        RegistrationService registrationService,
        User currentUser
    ) {
        try {
            System.out.print("Введите ID турнира: ");

            long tournamentId = Long.parseLong(
                scanner.nextLine().trim()
            );

            long registrationId = registrationService.create(
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
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
        } catch (SQLException exception) {
            System.out.println(
                "Не удалось создать заявку: "
                    + exception.getMessage()
            );
        }
    }
    private static void cancelRegistration(
        Scanner scanner,
        RegistrationService registrationService,
        User currentUser
    ) {
        try {
            System.out.print(
                "Введите ID заявки для отмены: "
            );

            long registrationId = Long.parseLong(
                scanner.nextLine().trim()
            );

            registrationService.cancelByUser(
                registrationId,
                currentUser.getId()
            );
            System.out.println("Заявка отменена");

        } catch (NumberFormatException exception) {
            System.out.println(
                "Ошибка: ID должен быть целым числом"
            );
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
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
        RegistrationService registrationService
    ) {
        try {
            System.out.print("Введите ID заявки: ");

            long registrationId = Long.parseLong(
               scanner.nextLine().trim()
            );

            System.out.print(
                "Новый статус "
                + "(PENDING, CONFIRMED, REJECTED, CANCELLED): "
            );

            RegistrationStatus newStatus = RegistrationStatus.valueOf(
                scanner.nextLine().trim().toUpperCase()
            );

            registrationService.changeStatus(
                registrationId,
                newStatus
            );
            System.out.println(
                "Статус заявки изменён на " + newStatus
            );

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
        } catch (BusinessException exception) {
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
    private static void findRegistrationById(
        Scanner scanner,
        RegistrationService registrationService
    ) {
        try {
            System.out.print("Введите ID заявки: ");
            long id = Long.parseLong(
                scanner.nextLine().trim()
            );

            Registration registration =
                registrationService.findById(id);

            System.out.println();
            System.out.println("=== ЗАЯВКА ===");
            System.out.println(registration);
        } catch (NumberFormatException exception) {
            System.out.println(
                "Ошибка: ID должен быть целым числом"
            );
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
        } catch (SQLException exception) {
            System.out.println(
                "Не удалось получить заявку: "
                    + exception.getMessage()
            );
        }
    }
    public static void searchRegistrationsByPlayer(
        Scanner scanner,
        RegistrationService registrationService
    ) {
        try{
            System.out.print("Введите часть ФИО игрока: ");
            String playerName = scanner.nextLine().trim();

            if(playerName.isEmpty()){
                System.out.println(
                    "Ошибка: строка поиска не может быть пустой"
                );
                return;
            }
            List<Registration> registrations =
                registrationService.searchByPlayerName(playerName);
            System.out.println();
            System.out.println(
                "===РЕЗУЛЬТАТ ПОИСКА ПО ФИО==="
            );

            if (registrations.isEmpty()){
                System.out.println("Заявки не найдены");
                return;
            }

            for (Registration registration : registrations ){
                System.out.println(registration);
            }
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
        } catch (SQLException exception){
            System.out.println(
                "Не удалось выполнить поиск: " + exception.getMessage()
            );
        }
    }
    private static void searchRegistrationsByTournament(
        Scanner scanner,
        RegistrationService registrationService
    ) {
        try{
            System.out.print(
                "Введите часть названия турнира:"
            );
            String tournamentTitle = scanner.nextLine().trim();

            if(tournamentTitle.isEmpty()){
                System.out.println("Ошибка: строка поиска не может быть пустой");
                return;
            }
            List<Registration> registrations = registrationService.searchByTournamentTitle(
                tournamentTitle
            );
            System.out.println();
            System.out.println(
                "===РЕЗУЛЬТАТ ПОИСКА ПО ТУРНИРАМ==="
            );
            if (registrations.isEmpty()){
                System.out.println("Заявки не найдены");
                return;
            }
            for (Registration registration : registrations){
                System.out.println(registration);
            }
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
        } catch (SQLException exception){
            System.out.println(
                "Не удалось выполнить поиск: "+exception.getMessage()
            );
        }
    }
    private static void filterRegistrationsByStatus(
        Scanner scanner,
        RegistrationService registrationService
    ) {
        try {
            System.out.print(
                "Статус (PENDING, CONFIRMED, REJECTED, CANCELLED): "
            );
            RegistrationStatus status = RegistrationStatus.valueOf(
                scanner.nextLine().trim().toUpperCase()
            );

            List<Registration> registrations =
                registrationService.filterByStatus(status);

            printRegistrations(
                "ФИЛЬТР ПО СТАТУСУ",
                registrations
            );
        } catch (IllegalArgumentException exception){
            System.out.println("Ошибка: неизвестный статус");
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
        } catch (SQLException exception){
            System.out.println(
                "Не удалось выполнить фильтрацию: " + exception.getMessage()
            );
        }
    }
    private static void filterRegistrationsByDate(
        Scanner scanner,
        RegistrationService registrationService
    ) {
        try {
            System.out.print("ДАТА от (ГГГГ-ММ-ДД): ");
            LocalDate from = LocalDate.parse(
                scanner.nextLine().trim()
            );
            System.out.print("ДАТА до (ГГГГ-ММ-ДД): ");
            LocalDate to = LocalDate.parse(
                scanner.nextLine().trim()
            );
            List<Registration> registrations =
                registrationService.filterByDateRange(from, to);

            printRegistrations(
                "ФИЛЬТР ПО ДИАПОЗОНУ ДАТ",
                registrations
            );

        } catch (DateTimeParseException exception) {
            System.out.println(
                "Ошибка: дата должна быть в формате ГГГГ-ММ-ДД"
            );
        } catch (IllegalArgumentException exception){
            System.out.println(
                "Не удалось выполнить фильтрацию: " + exception.getMessage()
            );
        } catch (BusinessException exception) {
            System.out.println(
                "Ошибка: " + exception.getMessage()
            );
        }
        catch (SQLException exception) {
            System.out.println(
                "Не удалось выполнить фильтрацию: "
                    + exception.getMessage()
            );
        }

    }

    private static void printRegistrations(
        String title,
        List<Registration> registrations
    ) {
        System.out.println();
        System.out.println("=== "+title+" ===");

        if (registrations.isEmpty()){
            System.out.println("Заявки не найдены");
            return;
        }
        for ( Registration registration : registrations){
            System.out.println(registration);
        }
    }
    private static void showStatistics(
        StatisticsService statisticsService

    ) {
        try {
            RegistrationStatistics statistics =
                statisticsService.calculate();

            printStatistics(statistics);
        } catch (SQLException exception) {
            System.out.println(
                "Не удалось получить статистику: "
                    + exception.getMessage()
            );
        }
    }
    private static void printStatistics (
        RegistrationStatistics statistics
    ) {
        System.out.println();
        System.out.println("=== СТАТИСТИКА ===");

        System.out.println(
            "Всего пользователей: "+statistics.getTotalUsers()
        );
        System.out.println(
            "Всего заявок: "+statistics.getTotalRegistrations()
        );
        System.out.println(
            "Всего турниров: "+statistics.getTotalTournaments()
        );
        System.out.println(
            "Заявки, ожидающие решения: "+statistics.getPending()
        );
        System.out.println(
            "Заявки подтверждённые: "+statistics.getConfirmed()
        );
        System.out.println(
            "Заявки отклонённые: "+statistics.getRejected()
        );
        System.out.println(
            "Заявки отменённые: "+statistics.getCancelled()
        );

    }
}
