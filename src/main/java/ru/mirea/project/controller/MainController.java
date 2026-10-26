package ru.mirea.project.controller;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TextField;
import java.time.format.DateTimeFormatter;
import javafx.geometry.Insets;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.layout.GridPane;
import javafx.util.StringConverter;

import javafx.stage.FileChooser;
import java.io.IOException;
import java.nio.file.Path;
import java.io.File;

import ru.mirea.project.util.ExcelExporter;
import ru.mirea.project.model.Tournament;
import ru.mirea.project.model.TournamentStatus;
import ru.mirea.project.model.User;
import ru.mirea.project.model.UserRole;
import ru.mirea.project.model.RegistrationStatistics;
import ru.mirea.project.model.Registration;
import ru.mirea.project.model.RegistrationStatus;
import ru.mirea.project.service.RegistrationService;
import ru.mirea.project.service.StatisticsService;
import ru.mirea.project.service.TournamentService;
import ru.mirea.project.service.UserService;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MainController{
    private final RegistrationService registrationService;
    private final UserService userService;
    private final User currentUser;
    private final TournamentService tournamentService;
    private final StatisticsService statisticsService;
    private final ExcelExporter excelExporter;
    private final ObservableList<Registration> registrations =
        FXCollections.observableArrayList();
    private final FilteredList<Registration> filteredRegistrations =
        new FilteredList<>(registrations, registration -> true);
    private final SortedList<Registration> sortedRegistrations =
        new SortedList<>(filteredRegistrations);
    private final ObservableList<Tournament> tournaments =
        FXCollections.observableArrayList();
    private final Map<Long, Long> activeRegistrationsByTournament =
        new HashMap<>();
    private static final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
    private static final DateTimeFormatter DATE_ONLY_FORMATTER =
        DateTimeFormatter.ofPattern("dd.MM.yyyy");

    @FXML
    private Label userLabel;
    @FXML
    private Label statusLabel;
    @FXML
    private Label profileFullNameLabel;
    @FXML
    private Label profileEmailLabel;
    @FXML
    private Label profileRoleLabel;
    @FXML
    private Label profileRatingLabel;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<RegistrationStatus> statusFilter;
    @FXML
    private ComboBox<Tournament> tournamentFilter;
    @FXML
    private TableView<Registration> registrationTable;
    @FXML
    private TableColumn<Registration, Long> idColumn;
    @FXML
    private TableColumn<Registration, String> playerColumn;
    @FXML
    private TableColumn<Registration, Integer> ratingColumn;
    @FXML
    private TableColumn<Registration, String> tournamentColumn;
    @FXML
    private TableColumn<
        Registration,
        RegistrationStatus> statusColumn;
    @FXML
    private TableColumn<
        Registration,
        LocalDateTime> dateColumn;
    @FXML
    private TableView<Tournament> tournamentsTable;
    @FXML
    private TableColumn<Tournament, Long> tournamentIdColumn;
    @FXML
    private TableColumn<Tournament, String> tournamentTitleColumn;
    @FXML
    private TableColumn<Tournament, String> timeControlColumn;
    @FXML
    private TableColumn<Tournament, LocalDate> startDateColumn;
    @FXML
    private TableColumn<Tournament, LocalDate> endDateColumn;
    @FXML
    private TableColumn<Tournament, Integer> capacityColumn;
    @FXML
    private TableColumn<Tournament, Long> occupiedColumn;
    @FXML
    private TableColumn<Tournament, Integer> availableColumn;
    @FXML
    private TableColumn<Tournament, String> ratingRangeColumn;
    @FXML
    private TableColumn<Tournament, TournamentStatus> tournamentStatusColumn;
    @FXML
    private Button addButton;
    @FXML
    private Button editButton;
    @FXML
    private Button deleteButton;
    @FXML
    private Button statisticsButton;
    @FXML
    private Button exportButton;
    @FXML
    private Button registerForTournamentButton;

    public MainController(
        RegistrationService registrationService,
        UserService userService,
        TournamentService tournamentService,
        StatisticsService statisticsService,
        ExcelExporter excelExporter,
        User currentUser
    ) {
        this.registrationService = registrationService;
        this.userService = userService;
        this.tournamentService = tournamentService;
        this.statisticsService = statisticsService;
        this.excelExporter = excelExporter;
        this.currentUser = currentUser;
    }

    @FXML
    private void initialize(){
        configureRoleAccess();
        configureProfile();
        idColumn.setCellValueFactory(cell->new SimpleObjectProperty<>(cell.getValue().getId()));
        playerColumn.setCellValueFactory(cell->new SimpleStringProperty(cell.getValue().getUser().getFullName()));
        ratingColumn.setCellValueFactory(cell->new SimpleObjectProperty<>(cell.getValue().getUser().getRating()));
        tournamentColumn.setCellValueFactory(cell->new SimpleStringProperty(cell.getValue().getTournament().getTitle()));
        statusColumn.setCellValueFactory(cell->new SimpleObjectProperty<>(cell.getValue().getStatus()));
        dateColumn.setCellValueFactory(cell->new SimpleObjectProperty<>(cell.getValue().getRegisteredAt()));
        dateColumn.setCellFactory(column->new TableCell<>(){
            @Override
            protected void updateItem(
                LocalDateTime date,
                boolean empty
            ) {
                super.updateItem(date, empty);

                if(empty || date ==null){
                    setText(null);
                } else {
                    setText(DATE_FORMATTER.format(date));
                }
            }
        });
        configureFilters();
        sortedRegistrations.comparatorProperty().bind(
            registrationTable.comparatorProperty()
        );
        registrationTable.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        tournamentsTable.setColumnResizePolicy(
            TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN
        );
        registrationTable.setItems(sortedRegistrations);
        configureTournamentTable();
        loadRegistrations();
        loadTournaments();
    }

    @FXML
    private void handleRefresh(){
        loadRegistrations();
        loadTournaments();
    }

    @FXML
    private void handleResetFilters() {
        searchField.clear();
        statusFilter.getSelectionModel().clearSelection();
        tournamentFilter.getSelectionModel().clearSelection();
        applyFilters();
    }
    @FXML
    private void handleAdd(){
        try{
            ComboBox<User> playerBox = null;

            if (isAdmin()) {
                playerBox = new ComboBox<>();
                playerBox.setPrefWidth(450);
                playerBox.getItems().setAll(
                    userService.findAll()
                        .stream()
                        .filter(user -> user.getRole() == UserRole.PLAYER)
                        .toList()
                );
            }

            ComboBox<Tournament> tournamentBox = new ComboBox<>();
            tournamentBox.setPrefWidth(450);
            tournamentBox.getItems().setAll(
                tournamentService.findAll().stream().filter(tournament->tournament.getStatus()==TournamentStatus.REGISTRATION_OPEN).toList()
            );
            GridPane form = new GridPane();
            form.setHgap(10);
            form.setVgap(10);
            form.setPadding(new Insets(20));

            int tournamentRow = 0;

            if (isAdmin()) {
                form.addRow(
                    0,
                    new Label("Игрок: "),
                    playerBox
                );
                tournamentRow = 1;
            }

            form.addRow(
                tournamentRow,
                new Label("Турнир: "),
                tournamentBox
            );
            ButtonType createButton = new ButtonType(
                "Создать",
                ButtonBar.ButtonData.OK_DONE
            );
            ButtonType cancelButton = new ButtonType(
                "Отмена",
                ButtonBar.ButtonData.CANCEL_CLOSE
            );

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Добавление заявки");
            dialog.setHeaderText(
                isAdmin()
                    ? "Выберите игрока и турнир"
                    : "Выберите турнир для регистрации"
            );
            dialog.initOwner(
                registrationTable.getScene().getWindow()
            );
            dialog.getDialogPane()
                .getButtonTypes()
                .addAll(cancelButton, createButton);

            dialog.getDialogPane().setContent(form);
            configureButtonOrder(dialog);

            ButtonType result = dialog.showAndWait()
                .orElse(cancelButton);

            if(result!=createButton){
                return;
            }

            User selectedUser = isAdmin()
                ? playerBox.getValue()
                : currentUser;
            Tournament selectedTournament = tournamentBox.getValue();
            if(selectedUser==null || selectedTournament==null){
                showError(
                    "Некорректные данные",
                    isAdmin()
                        ? "Необходимо выбрать игрока и турнир"
                        : "Необходимо выбрать турнир"
                );
                return;
            }

            registrationService.create(
                selectedUser.getId(),
                selectedTournament.getId()
            );
            loadRegistrations();
            loadTournaments();
        } catch (SQLException | RuntimeException exception){
            showError("Не удалось создать заявку", exception.getMessage());
        }
    }

    @FXML
    private void handleEdit() {
        if (!isAdmin()) {
            return;
        }

        Registration selectedRegistration = registrationTable
            .getSelectionModel()
            .getSelectedItem();

        if (selectedRegistration == null) {
            showError(
                "Заявка не выбрана",
                "Сначала выберите заявку в таблице"
            );
            return;
        }

        try {
            ComboBox<RegistrationStatus> statusBox =
                new ComboBox<>();

            statusBox.getItems().setAll(
                registrationService.allowedStatusesFor(
                    selectedRegistration.getStatus()
                )
            );

            if (statusBox.getItems().isEmpty()) {
                showError(
                    "Изменение невозможно",
                    "У завершённой заявки нельзя изменить статус"
                );
                return;
            }

            statusBox.getSelectionModel().selectFirst();
            statusBox.setPrefWidth(260);

            GridPane form = new GridPane();
            form.setHgap(10);
            form.setVgap(10);
            form.setPadding(new Insets(20));
            form.addRow(
                0,
                new Label("Текущий статус:"),
                new Label(selectedRegistration.getStatus().name())
            );
            form.addRow(
                1,
                new Label("Новый статус:"),
                statusBox
            );

            ButtonType saveButton = new ButtonType(
                "Сохранить",
                ButtonBar.ButtonData.OK_DONE
            );
            ButtonType cancelButton = new ButtonType(
                "Отмена",
                ButtonBar.ButtonData.CANCEL_CLOSE
            );

            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Изменение заявки");
            dialog.setHeaderText(
                "Заявка №" + selectedRegistration.getId()
                    + ": "
                    + selectedRegistration.getUser().getFullName()
            );
            dialog.initOwner(
                registrationTable.getScene().getWindow()
            );
            dialog.getDialogPane().setContent(form);
            dialog.getDialogPane()
                .getButtonTypes()
                .addAll(cancelButton, saveButton);
            configureButtonOrder(dialog);

            ButtonType result = dialog.showAndWait()
                .orElse(cancelButton);

            if (result != saveButton) {
                return;
            }

            registrationService.changeStatus(
                selectedRegistration.getId(),
                statusBox.getValue()
            );
            loadRegistrations();
            loadTournaments();
        } catch (SQLException | RuntimeException exception) {
            showError(
                "Не удалось изменить заявку",
                exception.getMessage()
            );
        }
    }

    @FXML
    private void handleDelete() {
        if (!isAdmin()) {
            return;
        }

        Registration selectedRegistration = registrationTable
            .getSelectionModel()
            .getSelectedItem();

        if (selectedRegistration == null) {
            showError(
                "Заявка не выбрана",
                "Сначала выберите заявку в таблице"
            );
            return;
        }

        if (!registrationService.canDelete(
            selectedRegistration.getStatus()
        )) {
            showError(
                "Удаление невозможно",
                "Удалить можно только активную заявку "
                    + "со статусом PENDING или CONFIRMED."
            );
            return;
        }

        ButtonType deleteButton = new ButtonType(
            "Удалить",
            ButtonBar.ButtonData.OK_DONE
        );
        ButtonType cancelButton = new ButtonType(
            "Отмена",
            ButtonBar.ButtonData.CANCEL_CLOSE
        );

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Удаление заявки");
        confirmation.setHeaderText(
            "Удалить заявку №" + selectedRegistration.getId() + "?"
        );
        confirmation.setContentText(
            "Игрок: "
                + selectedRegistration.getUser().getFullName()
                + "\nТурнир: "
                + selectedRegistration.getTournament().getTitle()
                + "\n\nЗаявка останется в истории со статусом CANCELLED."
        );
        confirmation.initOwner(
            registrationTable.getScene().getWindow()
        );
        confirmation.getDialogPane().setGraphic(null);
        confirmation.getButtonTypes().setAll(
            cancelButton,
            deleteButton
        );
        configureButtonOrder(confirmation);

        ButtonType result = confirmation.showAndWait()
            .orElse(cancelButton);

        if (result != deleteButton) {
            return;
        }

        try {
            registrationService.delete(
                selectedRegistration.getId()
            );
            loadRegistrations();
            loadTournaments();
        } catch (SQLException | RuntimeException exception) {
            showError(
                "Не удалось удалить заявку",
                exception.getMessage()
            );
        }
    }

    @FXML
    private void handleStatistics(){
        if (!isAdmin()) {
            return;
        }

        try{
            RegistrationStatistics statistics = statisticsService.calculate();

            GridPane content = new GridPane();
            content.setHgap(25);
            content.setVgap(15);
            content.setPadding(new Insets(20));

            content.addRow(
                0,
                new Label("Всего пользователей: "),
                new Label(
                    String.valueOf(statistics.getTotalUsers())
                )
            );
            content.addRow(
                1,
                new Label("Всего турниров: "),
                new Label(
                    String.valueOf(statistics.getTotalTournaments())
                )
            );
            content.addRow(
                2,
                new Label("Всего заявок: "),
                new Label(
                    String.valueOf(statistics.getTotalRegistrations())
                )
            );
            content.addRow(
                3,
                new Label("Заявок на рассмотрении: "),
                new Label(
                    String.valueOf(statistics.getPending())
                )
            );
            content.addRow(
                4,
                new Label("Заявок подтверждённых: "),
                new Label(
                    String.valueOf(statistics.getConfirmed())
                )
            );
            content.addRow(
                5,
                new Label("Заявок отклонённых: "),
                new Label(
                    String.valueOf(statistics.getRejected())
                )
            );
            content.addRow(
                6,
                new Label("Заявок отменённых: "),
                new Label(
                    String.valueOf(statistics.getCancelled())
                )
            );
            ButtonType closeButton = new ButtonType("Закрыть",ButtonBar.ButtonData.CANCEL_CLOSE);
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.setTitle("Статистика");
            dialog.setHeaderText("Статистика шахматного клуба");
            dialog.initOwner(
                registrationTable.getScene().getWindow()
            );
            dialog.getDialogPane().setContent(content);
            dialog.getDialogPane().getButtonTypes().setAll(closeButton);
            configureButtonOrder(dialog);
            dialog.showAndWait();


        }catch(SQLException exception){
            showError("Не удалось рассчитать статистику", exception.getMessage());
        }
    }

    @FXML
    private void handleExport(){
        if (!isAdmin()) {
            return;
        }

        FileChooser fileChooser = new FileChooser();

        fileChooser.setTitle("Сохранение заявок");
        fileChooser.setInitialFileName("registrations.xlsx");

        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "Книга Excel(*.xlsx)",
                "*.xlsx"
            )
        );

        File selectedFile = fileChooser.showSaveDialog(
            registrationTable.getScene().getWindow()
        );

        if(selectedFile==null){
            return;
        }
        try{
            Path outputFile = excelExporter.exportRegistrations(
                selectedFile.toPath(),
                sortedRegistrations.stream().toList()
            );
            statusLabel.setText("Экспорт завершен: "+ outputFile.getFileName());
        }catch(IOException exception){
            showError(
                "Не удалось сохранить Excel-файл",
                exception.getMessage()
            );
        }
    }

    @FXML
    private void handleRefreshTournaments() {
        loadTournaments();
    }

    @FXML
    private void handleRegisterForTournament() {
        if (isAdmin()) {
            return;
        }

        Tournament selectedTournament = tournamentsTable
            .getSelectionModel()
            .getSelectedItem();

        if (selectedTournament == null) {
            showError(
                "Турнир не выбран",
                "Сначала выберите турнир в таблице"
            );
            return;
        }

        try {
            registrationService.create(
                currentUser.getId(),
                selectedTournament.getId()
            );
            loadRegistrations();
            loadTournaments();
            showInformation(
                "Заявка создана",
                "Заявка на турнир «"
                    + selectedTournament.getTitle()
                    + "» отправлена на рассмотрение"
            );
        } catch (SQLException | RuntimeException exception) {
            showError(
                "Не удалось подать заявку",
                exception.getMessage()
            );
        }
    }

    private void loadRegistrations() {
        try {
            if (isAdmin()) {
                registrations.setAll(
                    registrationService.findAll()
                );
            } else {
                registrations.setAll(
                    registrationService.findByUserId(
                        currentUser.getId()
                    )
                );
            }
            applyFilters();
        } catch (SQLException exception) {
            showError(
                "Не удалось загрузить заявки",
                exception.getMessage()
            );
        }
    }

    private void loadTournaments() {
        try {
            activeRegistrationsByTournament.clear();
            tournaments.clear();

            for (Tournament tournament : tournamentService.findAll()) {
                activeRegistrationsByTournament.put(
                    tournament.getId(),
                    registrationService.countActiveByTournamentId(
                        tournament.getId()
                    )
                );
                tournaments.add(tournament);
            }

            tournamentsTable.refresh();
        } catch (SQLException exception) {
            showError(
                "Не удалось загрузить турниры",
                exception.getMessage()
            );
        }
    }

    private void configureTournamentTable() {
        tournamentIdColumn.setCellValueFactory(cell ->
            new SimpleObjectProperty<>(cell.getValue().getId())
        );
        tournamentTitleColumn.setCellValueFactory(cell ->
            new SimpleStringProperty(cell.getValue().getTitle())
        );
        timeControlColumn.setCellValueFactory(cell ->
            new SimpleStringProperty(
                cell.getValue().getTimeControl().name()
            )
        );
        startDateColumn.setCellValueFactory(cell ->
            new SimpleObjectProperty<>(cell.getValue().getStartDate())
        );
        endDateColumn.setCellValueFactory(cell ->
            new SimpleObjectProperty<>(cell.getValue().getEndDate())
        );
        configureDateColumn(startDateColumn);
        configureDateColumn(endDateColumn);
        capacityColumn.setCellValueFactory(cell ->
            new SimpleObjectProperty<>(
                cell.getValue().getMaxParticipants()
            )
        );
        occupiedColumn.setCellValueFactory(cell ->
            new SimpleObjectProperty<>(
                activeRegistrationsByTournament.getOrDefault(
                    cell.getValue().getId(),
                    0L
                )
            )
        );
        availableColumn.setCellValueFactory(cell -> {
            Tournament tournament = cell.getValue();
            long occupied = activeRegistrationsByTournament.getOrDefault(
                tournament.getId(),
                0L
            );
            int available = Math.max(
                0,
                tournament.getMaxParticipants() - (int) occupied
            );
            return new SimpleObjectProperty<>(available);
        });
        ratingRangeColumn.setCellValueFactory(cell ->
            new SimpleStringProperty(
                formatRatingRange(cell.getValue())
            )
        );
        tournamentStatusColumn.setCellValueFactory(cell ->
            new SimpleObjectProperty<>(cell.getValue().getStatus())
        );

        tournamentsTable.setItems(tournaments);
    }

    private void configureDateColumn(
        TableColumn<Tournament, LocalDate> column
    ) {
        column.setCellFactory(ignored -> new TableCell<>() {
            @Override
            protected void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                setText(
                    empty || date == null
                        ? null
                        : DATE_ONLY_FORMATTER.format(date)
                );
            }
        });
    }

    private String formatRatingRange(Tournament tournament) {
        Integer min = tournament.getMinRating();
        Integer max = tournament.getMaxRating();

        if (min == null && max == null) {
            return "Без ограничений";
        }
        if (min == null) {
            return "До " + max;
        }
        if (max == null) {
            return "От " + min;
        }
        return min + "–" + max;
    }

    private void configureFilters() {
        statusFilter.getItems().setAll(
            RegistrationStatus.values()
        );

        tournamentFilter.setConverter(new StringConverter<>() {
            @Override
            public String toString(Tournament tournament) {
                return tournament == null
                    ? ""
                    : tournament.getTitle();
            }

            @Override
            public Tournament fromString(String text) {
                return null;
            }
        });

        try {
            tournamentFilter.getItems().setAll(
                tournamentService.findAll()
            );
        } catch (SQLException exception) {
            showError(
                "Не удалось загрузить фильтры",
                exception.getMessage()
            );
        }

        searchField.textProperty().addListener(
            (observable, oldValue, newValue) -> applyFilters()
        );
        statusFilter.valueProperty().addListener(
            (observable, oldValue, newValue) -> applyFilters()
        );
        tournamentFilter.valueProperty().addListener(
            (observable, oldValue, newValue) -> applyFilters()
        );
    }

    private void configureRoleAccess() {
        userLabel.setText(
            "Пользователь: "
                + currentUser.getFullName()
                + " ("
                + currentUser.getRole()
                + ")"
        );

        boolean admin = isAdmin();
        setAvailable(addButton, admin);
        setAvailable(editButton, admin);
        setAvailable(deleteButton, admin);
        setAvailable(statisticsButton, admin);
        setAvailable(exportButton, admin);
        setAvailable(registerForTournamentButton, !admin);

        if (!admin) {
            searchField.setPromptText("Поиск по своим заявкам");
        }
    }

    private void configureProfile() {
        profileFullNameLabel.setText(currentUser.getFullName());
        profileEmailLabel.setText(currentUser.getEmail());
        profileRoleLabel.setText(currentUser.getRole().name());
        profileRatingLabel.setText(
            String.valueOf(currentUser.getRating())
        );
    }

    private void setAvailable(
        Button button,
        boolean available
    ) {
        button.setVisible(available);
        button.setManaged(available);
    }

    private boolean isAdmin() {
        return currentUser.getRole() == UserRole.ADMIN;
    }

    private void applyFilters() {
        String query = searchField.getText() == null
            ? ""
            : searchField.getText().trim().toLowerCase(Locale.ROOT);
        RegistrationStatus selectedStatus = statusFilter.getValue();
        Tournament selectedTournament = tournamentFilter.getValue();

        filteredRegistrations.setPredicate(registration -> {
            boolean matchesSearch = query.isEmpty()
                || registration.getUser().getFullName()
                    .toLowerCase(Locale.ROOT)
                    .contains(query)
                || registration.getTournament().getTitle()
                    .toLowerCase(Locale.ROOT)
                    .contains(query);

            boolean matchesStatus = selectedStatus == null
                || registration.getStatus() == selectedStatus;

            boolean matchesTournament = selectedTournament == null
                || registration.getTournament().getId()
                    == selectedTournament.getId();

            return matchesSearch
                && matchesStatus
                && matchesTournament;
        });

        statusLabel.setText(
            "Показано заявок: "
                + filteredRegistrations.size()
                + " из "
                + registrations.size()
        );
    }


    private void showError(
        String title,
        String message
    ) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        ButtonType closeButton = new ButtonType(
            "Закрыть",
            ButtonBar.ButtonData.OK_DONE
        );

        alert.setTitle("Ошибка");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.getDialogPane().setGraphic(null);
        alert.getDialogPane().getStyleClass().add("error-dialog");
        alert.getButtonTypes().setAll(closeButton);
        configureButtonOrder(alert);
        alert.showAndWait();
    }

    private void showInformation(
        String title,
        String message
    ) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        ButtonType closeButton = new ButtonType(
            "Закрыть",
            ButtonBar.ButtonData.OK_DONE
        );

        alert.setTitle("Готово");
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.getDialogPane().setGraphic(null);
        alert.getButtonTypes().setAll(closeButton);
        alert.initOwner(
            registrationTable.getScene().getWindow()
        );
        configureButtonOrder(alert);
        alert.showAndWait();
    }

    private void configureButtonOrder(Dialog<?> dialog) {
        String stylesheet = MainController.class
            .getResource("/css/app.css")
            .toExternalForm();

        if (!dialog.getDialogPane().getStylesheets().contains(stylesheet)) {
            dialog.getDialogPane().getStylesheets().add(stylesheet);
        }

        dialog.setOnShown(event -> {
            dialog.getDialogPane().applyCss();

            if (dialog.getDialogPane().lookup(".button-bar")
                instanceof ButtonBar buttonBar) {
                buttonBar.setButtonOrder("+OC");
            }
        });
    }
}
