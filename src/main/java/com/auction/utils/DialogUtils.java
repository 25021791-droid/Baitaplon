package utils;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class DialogUtils {

    // Hiển thị thông báo lỗi (Ví dụ: Mất kết nối mạng, Lỗi server)
    public static void showError(String title, String header, String content) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Hiển thị cảnh báo (Ví dụ: Giá đặt thấp hơn giá hiện tại, Phiên đã đóng)
    public static void showWarning(String title, String header, String content) {
        Alert alert = new Alert(AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    // Hiển thị thông báo thành công (Ví dụ: Đặt giá thành công)
    public static void showInfo(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}