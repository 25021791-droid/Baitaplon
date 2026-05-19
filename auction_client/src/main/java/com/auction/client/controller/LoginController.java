package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import com.auction.common.model.*;
import com.auction.client.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label messageLabel;

    @FXML private RadioButton bidderRadio;
    @FXML private RadioButton sellerRadio;
    @FXML private RadioButton adminRadio;
    @FXML private ToggleGroup roleGroup;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NetworkClientService.getInstance().setOnLoginSuccess(loggedInUser -> {
            javafx.application.Platform.runLater(() -> {
                try {
                    messageLabel.setText("Login success!");
                    messageLabel.setStyle("-fx-text-fill: green;");

                    UserSession.setUser(loggedInUser);

                    if (loggedInUser instanceof Admin) {
                        switchScene("/com/auction/Admin.fxml", "ADMIN");
                    } else if (loggedInUser instanceof Seller) {
                        switchScene("/com/auction/Seller.fxml", "SELLER");
                    } else {
                        switchScene("/com/auction/Bidder.fxml", "BIDDER");
                    }
                } catch (Exception e) {
                    System.err.println("[LỖI CHUYỂN TRANG]: Kiểm tra lại đường dẫn file FXML!");
                    e.printStackTrace();
                }
            });
        });

        NetworkClientService.getInstance().setOnLoginFail(errorMessage -> {
            javafx.application.Platform.runLater(() -> {
                messageLabel.setText(errorMessage);
                messageLabel.setStyle("-fx-text-fill: red;");
            });
        });
    }

    @FXML
    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setText("You have not entered your account or password.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        messageLabel.setText("Logging in...");
        messageLabel.setStyle("-fx-text-fill: black;");

        NetworkClientService.getInstance().login(user, pass);
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