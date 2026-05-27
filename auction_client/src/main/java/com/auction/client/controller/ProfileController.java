package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import com.auction.client.utils.UserSession;
import com.auction.common.model.User;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class ProfileController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField currentPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private Label profileMessageLabel;
    @FXML private Label passwordMessageLabel;

    private final NetworkClientService networkService = NetworkClientService.getInstance();
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentUser = UserSession.getUser();
        if (currentUser != null) {
            usernameField.setText(currentUser.getName());
            emailField.setText(currentUser.getEmail());
        }

        networkService.setOnProfileUpdateResult(isSuccess -> {
            if (isSuccess) {
                currentUser.setName(usernameField.getText().trim());
                currentUser.setEmail(emailField.getText().trim());
                showProfileMessage("Profile updated successfully.", "green");
            } else {
                showProfileMessage("Cannot update profile. Username or email may already exist.", "red");
            }
        });

        networkService.setOnPasswordChangeResult(isSuccess -> {
            if (isSuccess) {
                currentPasswordField.clear();
                newPasswordField.clear();
                confirmPasswordField.clear();
                showPasswordMessage("Password changed successfully.", "green");
            } else {
                showPasswordMessage("Cannot change password. Check your current password.", "red");
            }
        });
    }

    @FXML
    private void handleSaveProfile() {
        if (currentUser == null) {
            showProfileMessage("No user is logged in.", "red");
            return;
        }

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            showProfileMessage("Username and email are required.", "red");
            return;
        }

        if (username.contains(",") || email.contains(",")) {
            showProfileMessage("Username and email cannot contain commas.", "red");
            return;
        }

        if (!isValidEmail(email)) {
            showProfileMessage("Invalid email.", "red");
            return;
        }

        showProfileMessage("Saving profile...", "black");
        networkService.updateProfile(currentUser.getId(), username, email);
    }

    @FXML
    private void handleChangePassword() {
        if (currentUser == null) {
            showPasswordMessage("No user is logged in.", "red");
            return;
        }

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showPasswordMessage("All password fields are required.", "red");
            return;
        }

        if (currentPassword.contains(",") || newPassword.contains(",")) {
            showPasswordMessage("Passwords cannot contain commas.", "red");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showPasswordMessage("New password does not match.", "red");
            return;
        }

        showPasswordMessage("Changing password...", "black");
        networkService.changePassword(currentUser.getId(), currentPassword, newPassword);
    }

    @FXML
    private void handleClose() {
        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.close();
    }

    private boolean isValidEmail(String email) {
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
        return email.matches(emailRegex);
    }

    private void showProfileMessage(String message, String color) {
        profileMessageLabel.setText(message);
        profileMessageLabel.setStyle("-fx-text-fill: " + color + ";");
    }

    private void showPasswordMessage(String message, String color) {
        passwordMessageLabel.setText(message);
        passwordMessageLabel.setStyle("-fx-text-fill: " + color + ";");
    }
}
