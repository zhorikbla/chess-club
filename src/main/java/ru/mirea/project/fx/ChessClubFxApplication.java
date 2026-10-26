package ru.mirea.project.fx;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.Alert;


import ru.mirea.project.controller.MainController;
import ru.mirea.project.database.DatabaseManager;
import ru.mirea.project.repository.RegistrationRepository;
import ru.mirea.project.repository.TournamentRepository;
import ru.mirea.project.repository.UserRepository;
import ru.mirea.project.service.RegistrationService;
import ru.mirea.project.service.StatisticsService;
import ru.mirea.project.service.TournamentService;
import ru.mirea.project.service.UserService;
import ru.mirea.project.util.ExcelExporter;

import ru.mirea.project.controller.LoginController;
import ru.mirea.project.model.User;

import java.io.IOException;


public class ChessClubFxApplication extends Application {
    private Stage stage;
    private RegistrationService registrationService;
    private UserService userService;
    private TournamentService tournamentService;
    private StatisticsService statisticsService;
    private ExcelExporter excelExporter;

    @Override
    public void start(Stage stage) throws IOException{
        this.stage = stage;

        DatabaseManager databaseManager =
            DatabaseManager.fromProperties();

        UserRepository userRepository =
            new UserRepository(databaseManager);

        TournamentRepository tournamentRepository =
            new TournamentRepository(databaseManager);

        RegistrationRepository registrationRepository =
            new RegistrationRepository(databaseManager);

        registrationService =
            new RegistrationService(
                registrationRepository,
                userRepository,
                tournamentRepository
            );

        userService =
            new UserService(userRepository);

        tournamentService =
            new TournamentService(tournamentRepository);

        statisticsService =
            new StatisticsService(
                userRepository,
                tournamentRepository,
                registrationRepository
            );
        excelExporter = new ExcelExporter();

        stage.setTitle("Шахматный клуб");

        showLogin();

        stage.show();
    }

    public static void main(String[] args){
        launch(args);
    }
    private void applyStyles(Scene scene){
        scene.getStylesheets().add(
            ChessClubFxApplication.class.getResource("/css/app.css").toExternalForm()
        );
    }
    private void showLogin() throws IOException{
        FXMLLoader loader = new FXMLLoader(
            ChessClubFxApplication.class.getResource(
                "/fxml/login-view.fxml"
            )
        );

        loader.setControllerFactory(controllerClass->{
            if(controllerClass == LoginController.class){
                return new LoginController(
                    userService,
                    this::showMain
                );
            }
            throw new IllegalArgumentException(
                "Неизвестный контроллер: "+controllerClass.getName()
            );
        });

        Scene loginScene = new Scene(
            loader.load(),
            460,
            300
        );
        applyStyles(loginScene);

        stage.setMinWidth(460);
        stage.setMinHeight(300);
        stage.setScene(loginScene);
    }

    private void showMain(User currentUser){
        try{
            FXMLLoader loader = new FXMLLoader(
                ChessClubFxApplication.class.getResource(
                    "/fxml/main-view.fxml"
                )
            );

            loader.setControllerFactory(controllerClass->{
                if(controllerClass==MainController.class){
                    return new MainController(
                        registrationService,
                        userService,
                        tournamentService,
                        statisticsService,
                        excelExporter,
                        currentUser
                    );
                }
                throw new IllegalArgumentException(
                    "Неизвестный контроллер: "
                        + controllerClass.getName()
                );
            });
            Scene mainScene = new Scene(
                loader.load(),
                1100,
                650
            );
            applyStyles(mainScene);

            stage.setTitle("Шахматный клуб - "+currentUser.getFullName());

            stage.setMinWidth(800);
            stage.setMinHeight(500);
            stage.setScene(mainScene);
            stage.centerOnScreen();
        } catch(IOException exception){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Ошибка");
            alert.setHeaderText("Не удалось открыть главное окно");
            alert.setContentText(exception.getMessage());
            alert.getDialogPane().setGraphic(null);
            alert.getDialogPane().getStylesheets().add(
                ChessClubFxApplication.class
                    .getResource("/css/app.css")
                    .toExternalForm()
            );
            alert.getDialogPane().getStyleClass().add("error-dialog");
            alert.showAndWait();
        }
    }
}
