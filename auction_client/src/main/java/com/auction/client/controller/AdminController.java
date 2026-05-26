package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import com.auction.common.model.Auction;
import com.auction.common.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    // Bảng quản lý toàn bộ User trong hệ thống
    @FXML private TableView<User> tableUsers;

    // Bảng quản lý toàn bộ Phiên đấu giá
    @FXML private TableView<Auction> tableAllAuctions;

    @FXML private Button btnCancelAuction;
    @FXML private Button btnRefresh;

    private NetworkClientService networkService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.networkService = NetworkClientService.getInstance();

        btnRefresh.setOnAction(event -> loadSystemData());

        btnCancelAuction.setOnAction(event -> handleCancelSelectedAuction());

        // Lấy dữ liệu ngay khi mở màn hình
        loadSystemData();
    }

    private void loadSystemData() {
        System.out.println("Admin đang yêu cầu tải dữ liệu hệ thống...");
        // Bạn cần bổ sung các hàm này vào NetworkClientService:
        // networkService.requestAllUsers(...);
        // networkService.requestAllAuctions(...);
    }

    private void handleCancelSelectedAuction() {
        // Lấy dòng (Auction) đang được chọn trong bảng
        Auction selectedAuction = tableAllAuctions.getSelectionModel().getSelectedItem();

        if (selectedAuction == null) {
            showAlert("Thông báo", "Vui lòng chọn một phiên đấu giá trên bảng để hủy!");
            return;
        }

        // Gửi lệnh ADMIN_CANCEL_AUCTION lên Server
        System.out.println("Admin yêu cầu hủy phiên: " + selectedAuction.getId());
        // networkService.cancelAuction(selectedAuction.getId());
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/Profile.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.initOwner(btnRefresh.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Profile");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Cannot open profile screen.");
        }
    }

    private void showAlert(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
