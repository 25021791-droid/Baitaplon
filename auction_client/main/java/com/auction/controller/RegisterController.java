package com.auction.controller;

import com.auction.model.User;
import com.auction.service.UserService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class RegisterController {
    private final UserService userService = new UserService();

    @FXML
    private TextField usernameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    private Label messageLabel;

    @FXML
    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return email.matches(emailRegex);
    }

    @FXML
    private void handleRegister() {
        String user = usernameField.getText().trim();
        String email = emailField.getText().trim();
        String pass = passwordField.getText();
        String confirmPass = confirmPasswordField.getText();

        if (!isValidEmail(email)) {
            messageLabel.setText("Invalid Email!");
            return;
        }

        if (user.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() ) {
            messageLabel.setText("You have not entered your account or password.");
            return;
        }

        if (!pass.equals(confirmPass)) {
            messageLabel.setText("Password does not match.");
            return;
        }

        boolean loggedUser = userService.register(user, pass, email);

        if (loggedUser) {
            messageLabel.setText("Register success!");
            switchToAuction();
        } else {
            messageLabel.setText("Username is already in use.");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/Login.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();

            stage.setScene(new Scene(root));
            stage.setTitle("Login");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
            messageLabel.setText("Error");
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
