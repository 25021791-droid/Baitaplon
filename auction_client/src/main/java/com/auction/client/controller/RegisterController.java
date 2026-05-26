package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.util.Duration;
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
    private int registerRequestId = 0;
    private boolean waitingForRegisterResponse = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NetworkClientService.getInstance().setOnRegisterResult(isSuccess -> {
            if (!waitingForRegisterResponse) {
                return;
            }

            waitingForRegisterResponse = false;

            if (isSuccess) {
                showMessage("Register success! Please login.", "green");

                handleBackToLogin();
            } else {
                showMessage("Username or email is already in use.", "red");
            }
        });
    }

    private void showMessage(String message, String color) {
        messageLabel.setVisible(true);
        messageLabel.setManaged(true);
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: " + color + ";");
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
            showMessage("You have not entered your account or password.", "red");
            return;
        }

        if (!pass.equals(confirmPass)) {
            showMessage("Password does not match.", "red");
            return;
        }

        if (!isValidEmail(email)) {
            showMessage("Invalid Email!", "red");
            return;
        }

        if (!NetworkClientService.getInstance().isConnected()) {
            showMessage("Cannot connect to server. Start the server first.", "red");
            return;
        }

        String role = bidderRadio.isSelected() ? "BIDDER" : "SELLER";

        showMessage("Processing...", "black");
        waitingForRegisterResponse = true;
        int currentRequestId = ++registerRequestId;

        PauseTransition timeout = new PauseTransition(Duration.seconds(10));
        timeout.setOnFinished(event -> {
            if (waitingForRegisterResponse && currentRequestId == registerRequestId) {
                waitingForRegisterResponse = false;
                showMessage("Register timeout. Check server or database connection.", "red");
            }
        });
        timeout.play();

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
            showMessage("Error loading Login screen", "red");
        }
    }
}
