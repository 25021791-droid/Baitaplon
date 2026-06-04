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
    @FXML private Button loginButton;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        NetworkClientService.getInstance().setOnLoginSuccess(loggedInUser -> {
            javafx.application.Platform.runLater(() -> {
                try {
                    messageLabel.setVisible(true);
                    messageLabel.setText("Đăng nhập thành công!");
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
                messageLabel.setVisible(true);
                messageLabel.setText(errorMessage);
                messageLabel.setStyle("-fx-text-fill: red;");

                if (loginButton != null) {
                    loginButton.setDisable(false);
                }
            });
        });
    }

    @FXML
    private void handleLogin() {
        String user = usernameField.getText().trim();
        String pass = passwordField.getText();

        if (user.isEmpty() || pass.isEmpty()) {
            messageLabel.setVisible(true);
            messageLabel.setText("Bạn chưa nhập tài khoản hoặc mật khẩu của mình.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        if (user.contains(",") || pass.contains(",")) {
            messageLabel.setVisible(true);
            messageLabel.setText("Tài khoản hoặc mật khẩu không được chứa dấu phẩy.");
            messageLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        messageLabel.setVisible(true);
        messageLabel.setText("Đang đăng nhập ...");
        messageLabel.setStyle("-fx-text-fill: black;");

        if (loginButton != null) {
            loginButton.setDisable(true);
        }

        NetworkClientService.getInstance().login(user, pass);
    }

    private void switchScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setMaximized(true);
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
            stage.setTitle("Đăng kí");
            stage.show();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}