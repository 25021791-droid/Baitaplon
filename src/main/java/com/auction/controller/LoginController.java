package com.auction.controller;
import com.auction.service.UserService;
import com.auction.exception.*;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;

public class LoginController {
    private UserService userService = new UserService();
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

        try {
            userService.login(user, pass);

            messageLabel.setText("Login success!");

            switchToAuction();

        } catch (AuthenticationException e) {
            messageLabel.setText(e.getMessage());
        }
    }

    private void switchToAuction() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/Auction.fxml")
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