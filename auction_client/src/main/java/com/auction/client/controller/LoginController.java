package com.auction.client.controller;

import com.auction.client.service.ClientService;
import com.auction.common.model.User;
import com.auction.service.UserService;
import com.auction.client.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label messageLabel;

    @FXML
    private RadioButton bidderRadio;

    @FXML
    private RadioButton sellerRadio;

    @FXML
    private RadioButton adminRadio;

    @FXML
    private ToggleGroup roleGroup;

    @FXML
    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("You have not entered your account or password.");
            return;
        }

        String selectedRole;
        if (adminRadio.isSelected()) {
            selectedRole = "ADMIN";
        } else if (sellerRadio.isSelected()) {
            selectedRole = "SELLER";
        } else {
            selectedRole = "BIDDER";
        }

        boolean isSuccess = ClientService.getInstance().login(user, pass);

        if (isSuccess != null) {
            messageLabel.setText("Login success!");

            UserSession.setUser(isSuccess);

            switch (selectedRole) {
                case "ADMIN":
                    switchScene("/com/auction/Admin.fxml", "ADMIN");
                    break;
                case "SELLER":
                    switchScene("/com/auction/Seller.fxml", "SELLER");
                    break;
                case "BIDDER":
                default:
                    switchScene("/com/auction/Auction.fxml", "BIDDER");
                    break;
            }
        } else {
            messageLabel.setText("Incorrect account or password.");
        }
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();

        } catch (IOException e) {
            System.err.println("Lỗi khi load giao diện: " + fxmlPath);
            e.printStackTrace();
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