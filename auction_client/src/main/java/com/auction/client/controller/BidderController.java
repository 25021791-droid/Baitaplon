package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import com.auction.common.model.*;
import com.auction.client.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class BidderController implements Initializable {

    @FXML private TextField bidField;
    @FXML private Label resultLabel;
    @FXML private Label lblUsername;
    @FXML private Label lblCurrentPrice; // Thêm Label hiển thị giá hiện tại

    private Bidder bidder;
    private Auction auction;
    private NetworkClientService networkService;

    @Override
    public void initialize(URL location, ResourceBundle resource) {
        this.networkService = NetworkClientService.getInstance();
        User user = UserSession.getUser();

        if (user != null) {
            lblUsername.setText("User: " + user.getName());
            if (user instanceof Bidder) {
                this.bidder = (Bidder) user;
            }
        } else {
            this.bidder = new Bidder(1, "dat", "a@gmail.com", 1000.0);
            lblUsername.setText("User: " + bidder.getName());
        }

        Item item = new Electronics(1, "Laptop");
        this.auction = new Auction(item, 100);
        lblCurrentPrice.setText("Giá hiện tại: $" + auction.getCurrentPrice());

        networkService.setOnBidResult((isSuccess) -> {
            if (isSuccess) {
                resultLabel.setText("Đặt giá thành công!");
                resultLabel.setStyle("-fx-text-fill: green;");
            } else {
                resultLabel.setText("Đặt giá thất bại (Có thể giá quá thấp)!");
                resultLabel.setStyle("-fx-text-fill: red;");
            }
        });

        networkService.setOnNewBidBroadcast((message) -> {
            String[] parts = message.split(",");
            long incomingAuctionId = Long.parseLong(parts[1]);
            double newPrice = Double.parseDouble(parts[2]);

            if (incomingAuctionId == this.auction.getId()) {
                this.auction.setCurrentPrice(newPrice);
                lblCurrentPrice.setText("Giá hiện tại: $" + newPrice);
                resultLabel.setText("Vừa có người đặt giá mới!");
                resultLabel.setStyle("-fx-text-fill: orange;");
            }
        });
    }

    @FXML
    private void handleBid() {
        try {
            double amount = Double.parseDouble(bidField.getText());
            if (amount > bidder.getBalance()) {
                resultLabel.setText("Bạn không đủ tiền trong ví!");
                resultLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            resultLabel.setText("Đang xử lý...");
            networkService.placeBid(auction.getId(), bidder.getId(), amount);

        } catch (NumberFormatException e) {
            resultLabel.setText("Vui lòng nhập một số hợp lệ!");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/com/auction/Login.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) lblUsername.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
            UserSession.cleanUserSession();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}
