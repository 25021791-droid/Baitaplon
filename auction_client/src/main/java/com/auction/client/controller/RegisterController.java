package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private RadioButton bidderRadio;
    @FXML private RadioButton sellerRadio;
    @FXML private Label messageLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NetworkClientService.getInstance().setOnRegisterResult(isSuccess -> {
            if (isSuccess) {
                messageLabel.setStyle("-fx-text-fill: green;");
                messageLabel.setText("Register success! Please login.");

                handleBackToLogin();
            } else {
                messageLabel.setStyle("-fx-text-fill: red;");
                messageLabel.setText("Username or email is already in use.");
            }
        });
    }

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
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!pass.equals(confirmPass)) {
            messageLabel.setText("Password does not match.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (!isValidEmail(email)) {
            messageLabel.setText("Invalid Email!");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        String role = bidderRadio.isSelected() ? "BIDDER" : "SELLER";

        messageLabel.setText("Processing...");
        messageLabel.setStyle("-fx-text-fill: black;");

        NetworkClientService.getInstance().register(user, pass, email, role);
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
            messageLabel.setText("Error loading Login screen");
        }
    }
}
