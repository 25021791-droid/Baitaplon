package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import com.auction.common.model.*;
import com.auction.client.utils.UserSession;
import com.auction.client.utils.DialogUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class BiddingRoomController {

    @FXML private Label productNameLabel;
    @FXML private Label currentPriceLabel;
    @FXML private Label timeRemainingLabel;
    @FXML private TextField bidAmountInput;
    @FXML private Button placeBidButton;
    @FXML private VBox chartContainer;

    private RealtimeChartController chartController;
    private NetworkClientService networkService;

    private Auction currentAuction;
    private User currentUser;
    private double currentHighestPrice = 0.0;

    @FXML
    public void initialize() {
        // 1. Lấy instance dịch vụ mạng và thông tin session người dùng hiện tại
        this.networkService = NetworkClientService.getInstance();
        this.currentUser = UserSession.getUser();

        // 2. Khởi tạo biểu đồ thời gian thực
        if (chartContainer != null) {
            chartController = new RealtimeChartController();
            chartContainer.getChildren().add(chartController.createChart());
        }

        // 3. Đăng ký sự kiện nút bấm đặt giá công khai
        if (placeBidButton != null) {
            placeBidButton.setOnAction(e -> handlePlaceBid());
        }

        // 4. 🔥 QUAN TRỌNG: Lắng nghe Broadcast "NEW_BID" từ Server khi có ai đó đặt giá thành công
        // Dữ liệu gói tin truyền từ Server dạng: "NEW_BID,auctionId,newPrice,bidderName,timeLeft"
        networkService.setOnNewBidBroadcast(message -> {
            try {
                String[] parts = message.split(",");
                long bcastAuctionId = Long.parseLong(parts[1]);

                // Chỉ cập nhật UI nếu gói tin này thuộc về phòng đấu giá sản phẩm người dùng đang xem
                if (currentAuction != null && bcastAuctionId == currentAuction.getId()) {
                    double newPrice = Double.parseDouble(parts[2]);
                    String topBidder = parts[3];
                    String timeLeft = parts.length > 4 ? parts[4] : "Đang diễn ra";

                    // Gọi hàm cập nhật trạng thái UI và nạp dữ liệu lên biểu đồ biểu diễn
                    updateBiddingState(newPrice, topBidder, timeLeft);
                }
            } catch (Exception ex) {
                System.err.println("[BiddingRoom] Lỗi phân tích gói tin NEW_BID: " + ex.getMessage());
            }
        });

        // 5. Lắng nghe kết quả cược của CHÍNH MÌNH khi nhấn nút đặt giá
        networkService.setOnBidResult(isSuccess -> {
            Platform.runLater(() -> {
                if (isSuccess) {
                    DialogUtils.showInfo("Thành công", "Lượt đặt giá của bạn đã được hệ thống ghi nhận!");
                    bidAmountInput.clear();
                } else {
                    DialogUtils.showError("Thất bại", "Đặt giá không thành công", "Giá của bạn có thể đã chậm hơn người khác hoặc tài khoản không đủ số dư.");
                }
            });
        });
    }

    /**
     * 🔥 BỔ SUNG: Hàm nhận dữ liệu ban đầu từ màn hình danh sách truyền sang khi bấm vào Card
     */
    public void setAuctionData(Auction auction) {
        this.currentAuction = auction;
        this.currentHighestPrice = auction.getCurrentPrice();

        if (productNameLabel != null) {
            productNameLabel.setText(auction.getItem().getName());
        }
        if (currentPriceLabel != null) {
            currentPriceLabel.setText(String.format("%,.0f VNĐ", currentHighestPrice));
        }

        // Đưa giá khởi điểm ban đầu lên biểu đồ
        if (chartController != null) {
            chartController.onNewBidReceived(currentHighestPrice);
        }
    }

    private void handlePlaceBid() {
        if (currentAuction == null) {
            DialogUtils.showWarning("Cảnh báo", "Lỗi dữ liệu", "Không tìm thấy thông tin phiên đấu giá hiện hành!");
            return;
        }

        try {
            double bidAmount = Double.parseDouble(bidAmountInput.getText());

            // Validate logic nghiệp vụ trên UI trước khi tốn băng thông đẩy lệnh đi
            if (bidAmount <= currentHighestPrice) {
                DialogUtils.showWarning("Lỗi đặt giá", "Giá không hợp lệ", "Giá đấu phải cao hơn giá hiện tại!");
                return;
            }

            if (currentUser instanceof Bidder) {
                double userBalance = ((Bidder) currentUser).getBalance();
                if (bidAmount > userBalance) {
                    DialogUtils.showWarning("Lỗi số dư", "Ví không đủ tiền", "Số dư tài khoản không đủ để thực hiện lượt đặt giá này.");
                    return;
                }
            }

            // Bắn tín hiệu mạng thời gian thực lên TCP Server xử lý
            networkService.placeBid(currentAuction.getId(), currentUser.getId(), bidAmount);

        } catch (NumberFormatException ex) {
            DialogUtils.showError("Lỗi nhập liệu", "Định dạng sai", "Vui lòng nhập số tiền hợp lệ (ví dụ: 1500000).");
        } catch (Exception ex) {
            DialogUtils.showError("Lỗi hệ thống", "Có lỗi xảy ra", ex.getMessage());
        }
    }

    /**
     * Phương thức cập nhật trạng thái phòng đấu giá
     */
    public void updateBiddingState(double newPrice, String topBidder, String timeLeft) {
        Platform.runLater(() -> {
            this.currentHighestPrice = newPrice;
            if (currentPriceLabel != null) {
                currentPriceLabel.setText(String.format("%,.0f VNĐ (Bởi: %s)", newPrice, topBidder));
            }
            if (timeRemainingLabel != null) {
                timeRemainingLabel.setText(timeLeft);
            }
        });

        // Đẩy số liệu mới vào đồ thị Realtime
        if (chartController != null) {
            chartController.onNewBidReceived(newPrice);
        }
    }
}