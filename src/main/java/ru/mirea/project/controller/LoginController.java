package ru.mirea.project.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import ru.mirea.project.model.User;
import ru.mirea.project.service.UserService;

import java.sql.SQLException;
import java.util.function.Consumer;

public class LoginController{
    private final UserService userService;
    private final Consumer<User> onLoginSuccess;

    @FXML
    private TextField emailField;

    @FXML
    private Label errorLabel;

    public LoginController(
        UserService userService,
        Consumer<User> onLoginSuccess
    ) {
        this.userService = userService;
        this.onLoginSuccess = onLoginSuccess;
    }

    @FXML
    private void handleLogin(){
        String email = emailField.getText().trim();
        if(email.isEmpty()){
            showError("Введите email");
            return;
        }
        try{
            User user = userService.findByEmail(email);
            if(user==null){
                showError("Пользователь с таким email не найден");
                return;
            }

            errorLabel.setVisible(false);
            errorLabel.setManaged(false);

            onLoginSuccess.accept(user);
        } catch(SQLException exception){
            showError("Не удалось выполнить вход: " + exception.getMessage());
        }
    }
    private void showError(String message){
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }
}
