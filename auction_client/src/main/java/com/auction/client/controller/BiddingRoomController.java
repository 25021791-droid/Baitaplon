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
        
        this.networkService = NetworkClientService.getInstance();
        this.currentUser = UserSession.getUser();

        
        if (chartContainer != null) {
            chartController = new RealtimeChartController();
            chartContainer.getChildren().add(chartController.createChart());
        }

        
        if (placeBidButton != null) {
            placeBidButton.setOnAction(e -> handlePlaceBid());
        }

        
        
        networkService.setOnNewBidBroadcast(message -> {
            try {
                String[] parts = message.split(",");
                long bcastAuctionId = Long.parseLong(parts[1]);

                
                if (currentAuction != null && bcastAuctionId == currentAuction.getId()) {
                    double newPrice = Double.parseDouble(parts[2]);
                    String topBidder = parts[3];
                    String timeLeft = parts.length > 4 ? parts[4] : "Đang diễn ra";

                    
                    updateBiddingState(newPrice, topBidder, timeLeft);
                }
            } catch (Exception ex) {
                System.err.println("[BiddingRoom] Lỗi phân tích gói tin NEW_BID: " + ex.getMessage());
            }
        });

        
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

    
    public void setAuctionData(Auction auction) {
        this.currentAuction = auction;
        this.currentHighestPrice = auction.getCurrentPrice();

        if (productNameLabel != null) {
            productNameLabel.setText(auction.getItem().getName());
        }
        if (currentPriceLabel != null) {
            currentPriceLabel.setText(String.format("%,.0f VNĐ", currentHighestPrice));
        }

        
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

            
            networkService.placeBid(currentAuction.getId(), currentUser.getId(), bidAmount);

        } catch (NumberFormatException ex) {
            DialogUtils.showError("Lỗi nhập liệu", "Định dạng sai", "Vui lòng nhập số tiền hợp lệ (ví dụ: 1500000).");
        } catch (Exception ex) {
            DialogUtils.showError("Lỗi hệ thống", "Có lỗi xảy ra", ex.getMessage());
        }
    }

    
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

        
        if (chartController != null) {
            chartController.onNewBidReceived(newPrice);
        }
    }
}