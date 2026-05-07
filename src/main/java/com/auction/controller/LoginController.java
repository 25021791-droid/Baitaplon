package com.auction.controller;

import com.auction.model.User;
import com.auction.service.UserService;
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
        String user = usernameField.getText();
        String pass = passwordField.getText();
        User loggedUser = userService.login(user, pass);

        if (loggedUser != null) {
            messageLabel.setText("Login success!");
            switchToAuction();
        } else {
            messageLabel.setText("Sai tài khoản hoặc mật khẩu!");
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