package com.auction.controller;

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
    private RadioButton bidderRadio;

    @FXML
    private RadioButton sellerRadio;

    @FXML
    private Label messageLabel;

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

        if (user.isEmpty() || email.isEmpty() || pass.isEmpty() || confirmPass.isEmpty() ) {
            messageLabel.setText("You have not entered your account or password.");
            return;
        }

        String role = bidderRadio.isSelected() ? "BIDDER" : "SELLER";

        if (!pass.equals(confirmPass)) {
            messageLabel.setText("Password does not match.");
            return;
        }

        if (!isValidEmail(email)) {
            messageLabel.setText("Invalid Email!");
            return;
        }

        boolean loggedUser = userService.register(user, pass, email);

        if (loggedUser) {
            messageLabel.setStyle("-fx-text-fill: green;");
            messageLabel.setText("Register success! Please login.");
            handleBackToLogin();
        } else {
            messageLabel.setStyle("-fx-text-fill: red;");
            messageLabel.setText("Username or email is already in use.");
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
