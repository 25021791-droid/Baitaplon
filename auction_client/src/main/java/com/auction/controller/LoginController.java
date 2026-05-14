package com.auction.controller;

import com.auction.service.UserService;
import com.auction.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class LoginController {
    private final UserService userService = new UserService();

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("You have not entered your account or password.");
            return;
        }

        com.auction.model.User isSuccess = userService.login(user, pass);

        if (isSuccess != null) {
            messageLabel.setText("Login success!");

            UserSession.setUser(isSuccess);

            switchToAuction();
        } else {
            messageLabel.setText("Incorrect account or password.");
        }
    }

    private void switchToAuction() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/auction/Auction.fxml")
            );

            Parent root = loader.load();
            Stage stage = (Stage) usernameField.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Auction");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToRegister() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/Register.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Register");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}