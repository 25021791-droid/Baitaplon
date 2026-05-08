package controllers;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import utils.DialogUtils; // Nhập lớp DialogUtils bạn vừa tạo

public class BiddingRoomController {

    @FXML private Label productNameLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label timeRemainingLabel;
    @FXML private TextField bidAmountInput;
    @FXML private Button placeBidButton;
    @FXML private VBox chartContainer; // Nơi chứa biểu đồ

    private RealtimeChartController chartController;
    private double currentHighestPrice = 0.0;

    @FXML
    public void initialize() {
        // 1. Khởi tạo biểu đồ và gắn vào giao diện nếu chartContainer đã được tạo
        if (chartContainer != null) {
            chartController = new RealtimeChartController();
            chartContainer.getChildren().add(chartController.createChart());
        }

        // 2. Lắng nghe sự kiện click nút Đặt giá (nếu nút đã được tạo)
        if (placeBidButton != null) {
            placeBidButton.setOnAction(e -> handlePlaceBid());
        }
    }

    private void handlePlaceBid() {
        try {
            double bidAmount = Double.parseDouble(bidAmountInput.getText());

            // Validate cơ bản trên UI trước khi gửi xuống Server
            if (bidAmount <= currentHighestPrice) {
                DialogUtils.showWarning("Lỗi đặt giá", "Giá không hợp lệ", "Giá đấu phải cao hơn giá hiện tại!");
                return;
            }

            // TODO: Báo Người 1 viết API gửi dữ liệu xuống Server ở đây

            // Giả lập thành công tạm thời cho UI testing
            DialogUtils.showInfo("Thành công", "Đã gửi yêu cầu đặt giá!");

        } catch (NumberFormatException ex) {
            DialogUtils.showError("Lỗi nhập liệu", "Định dạng sai", "Vui lòng nhập số tiền hợp lệ.");
        } catch (Exception ex) {
            DialogUtils.showError("Lỗi hệ thống", "Có lỗi xảy ra", ex.getMessage());
        }
    }

    // Phương thức này backend sẽ gọi ngược lại qua Observer khi có người đặt giá thành công
    public void updateBiddingState(double newPrice, String topBidder, String timeLeft) {
        Platform.runLater(() -> {
            this.currentHighestPrice = newPrice;
            if (currentPriceLabel != null) currentPriceLabel.setText(String.format("%,.0f VNĐ (Bởi: %s)", newPrice, topBidder));
            if (timeRemainingLabel != null) timeRemainingLabel.setText(timeLeft);
        });

        // Cập nhật biểu đồ
        if (chartController != null) {
            chartController.onNewBidReceived(newPrice);
        }
    }
}