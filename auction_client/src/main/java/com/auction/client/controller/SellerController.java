package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import com.auction.common.model.Auction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;

public class SellerController implements Initializable {

    @FXML private TextField txtItemName;
    @FXML private TextField txtStartPrice;
    @FXML private Button btnCreateAuction;

    // Bảng để Seller xem các mặt hàng mình đang bán
    @FXML private TableView<Auction> tableMyAuctions;

    private NetworkClientService networkService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.networkService = NetworkClientService.getInstance();

        // Sự kiện khi bấm nút Tạo phiên đấu giá
        btnCreateAuction.setOnAction(event -> handleCreateAuction());

        // (Tùy chọn) Lắng nghe phản hồi từ Server khi tạo thành công
        // networkService.setOnAuctionCreatedResult(isSuccess -> { ... });
    }

    private void handleCreateAuction() {
        String itemName = txtItemName.getText().trim();
        String priceStr = txtStartPrice.getText().trim();

        if (itemName.isEmpty() || priceStr.isEmpty()) {
            showAlert("Lỗi nhập liệu", "Vui lòng nhập đầy đủ tên sản phẩm và giá khởi điểm!");
            return;
        }

        try {
            double startPrice = Double.parseDouble(priceStr);
            if (startPrice <= 0) {
                showAlert("Lỗi giá", "Giá khởi điểm phải lớn hơn 0!");
                return;
            }

            // Gửi lệnh lên Server (Bạn cần tự bổ sung hàm này vào NetworkClientService)
            // Cú pháp gợi ý: networkService.createAuction(itemName, startPrice);
            System.out.println("Đang gửi yêu cầu tạo phiên: " + itemName + " - Giá: " + startPrice);

            // Tạm thời hiển thị thông báo giả lập
            showAlert("Thành công", "Đã gửi yêu cầu tạo phiên đấu giá lên Server!");

            // Xóa trắng form sau khi tạo
            txtItemName.clear();
            txtStartPrice.clear();

        } catch (NumberFormatException e) {
            showAlert("Lỗi nhập liệu", "Giá khởi điểm phải là một con số hợp lệ!");
        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}