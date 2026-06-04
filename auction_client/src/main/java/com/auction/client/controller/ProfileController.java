package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import com.auction.client.utils.UserSession;
import com.auction.common.model.User;
import javafx.application.Platform;
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
            Platform.runLater(() -> {
                if (isSuccess) {
                    
                    currentUser.setName(usernameField.getText().trim());
                    currentUser.setEmail(emailField.getText().trim());

                    
                    UserSession.setUser(currentUser);

                    showProfileMessage("Hồ sơ đã được cập nhật thành công.", "green");
                } else {
                    showProfileMessage("Cập nhật thất bại. Tên đăng nhập hoặc email có thể đã tồn tại!", "red");
                }
            });
        });

        
        networkService.setOnPasswordChangeResult(isSuccess -> {
            Platform.runLater(() -> {
                if (isSuccess) {
                    currentPasswordField.clear();
                    newPasswordField.clear();
                    confirmPasswordField.clear();
                    showPasswordMessage("Đổi mật khẩu thành công.", "green");
                } else {
                    showPasswordMessage("Mật khẩu cũ không chính xác. Vui lòng thử lại!", "red");
                }
            });
        });
    }

    @FXML
    private void handleSaveProfile() {
        if (currentUser == null) {
            showProfileMessage("Hết phiên làm việc. Vui lòng đăng nhập lại.", "red");
            return;
        }

        String username = usernameField.getText().trim();
        String email = emailField.getText().trim();

        if (username.isEmpty() || email.isEmpty()) {
            showProfileMessage("Tên người dùng và email không được để trống.", "red");
            return;
        }

        if (username.contains(",") || email.contains(",")) {
            showProfileMessage("Tên hoặc email không được chứa ký tự dấu phẩy (,).", "red");
            return;
        }

        if (!isValidEmail(email)) {
            showProfileMessage("Định dạng email không hợp lệ.", "red");
            return;
        }

        showProfileMessage("Đang gửi yêu cầu lưu cấu hình...", "black");
        networkService.updateProfile(currentUser.getId(), username, email);
    }

    @FXML
    private void handleChangePassword() {
        if (currentUser == null) {
            showPasswordMessage("Hết phiên làm việc. Vui lòng đăng nhập lại.", "red");
            return;
        }

        String currentPassword = currentPasswordField.getText();
        String newPassword = newPasswordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            showPasswordMessage("Vui lòng điền đầy đủ thông tin các trường mật khẩu.", "red");
            return;
        }

        if (currentPassword.contains(",") || newPassword.contains(",")) {
            showPasswordMessage("Mật khẩu không được chứa ký tự dấu phẩy (,).", "red");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            showPasswordMessage("Mật khẩu mới xác nhận không trùng khớp!", "red");
            return;
        }

        showPasswordMessage("Đang thực hiện đổi mật khẩu...", "black");
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