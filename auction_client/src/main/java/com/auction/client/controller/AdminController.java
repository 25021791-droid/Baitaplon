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
        // ========== SETUP CỘT CHO BẢNG CHỜ DUYỆT ==========
        setupPendingAuctionsTable();
        // ========== NÚT LÀM MỚI ==========
        btnRefresh.setOnAction(event -> {
            loadSystemData();
            networkService.requestPendingAuctions(); // Load cả danh sách chờ duyệt
        });
        networkService.setOnPendingAuctionsReceived(auctions -> {
            System.out.println("[AdminController] ===== CALLBACK NHẬN DỮ LIỆU =====");
            System.out.println("[AdminController] Số auction: " + auctions.size());
            for (Auction a : auctions) {
                System.out.println("[AdminController] Auction: ID=" + a.getId() + " Name=" + a.getItem().getName());
            }

            Platform.runLater(() -> {
                System.out.println("[AdminController] Đang update TableView...");
                tablePendingAuctions.getItems().clear();
                tablePendingAuctions.getItems().addAll(auctions);
                System.out.println("[AdminController] TableView có " + tablePendingAuctions.getItems().size() + " items");
            });
        });

        networkService.setOnPendingAuctionsReceived(auctions -> {
            System.out.println("[Admin Client] Nhận được " + auctions.size() + " auction pending");
            Platform.runLater(() -> {
                tablePendingAuctions.getItems().clear();
                tablePendingAuctions.getItems().addAll(auctions);
                System.out.println("[Admin Client] Đã cập nhật TableView");
            });
        });
        // ========== NÚT HỦY AUCTION ==========
        btnCancelAuction.setOnAction(event -> handleCancelSelectedAuction());

        // ========== NÚT DUYỆT AUCTION ==========
        btnApprove.setOnAction(event -> handleApprove());

        // ========== CALLBACK: Nhận danh sách auction chờ duyệt ==========
        networkService.setOnPendingAuctionsReceived(auctions -> {
            Platform.runLater(() -> {
                tablePendingAuctions.getItems().clear();
                tablePendingAuctions.getItems().addAll(auctions);
                System.out.println("[Admin] Đã load " + auctions.size() + " auction chờ duyệt");
            });
        });

        // ========== CALLBACK: Kết quả duyệt auction ==========
        networkService.setOnApproveAuctionResult(success -> {
            Platform.runLater(() -> {
                if (success) {
                    showAlert("Thành công", "Đã duyệt phiên đấu giá!");
                    networkService.requestPendingAuctions(); // Refresh danh sách
                } else {
                    showAlert("Lỗi", "Không thể duyệt phiên đấu giá!");
                }
            });
        });

        // ========== LOAD DỮ LIỆU BAN ĐẦU ==========
        loadSystemData();
        networkService.requestPendingAuctions(); // Load danh sách chờ duyệt
    }
    private void setupPendingAuctionsTable() {
        // Xóa cột cũ (nếu có)
        tablePendingAuctions.getColumns().clear();

        // Tạo cột ID
        javafx.scene.control.TableColumn<Auction, String> colId = new javafx.scene.control.TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> {
            Long id = cellData.getValue().getId();
            return new javafx.beans.property.SimpleStringProperty(id != null ? id.toString() : "N/A");
        });
        colId.setPrefWidth(75);

        // Tạo cột Tên Sản Phẩm
        javafx.scene.control.TableColumn<Auction, String> colName = new javafx.scene.control.TableColumn<>("Tên Sản Phẩm");
        colName.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getItem().getName();
            return new javafx.beans.property.SimpleStringProperty(name != null ? name : "N/A");
        });
        colName.setPrefWidth(250);

        // Tạo cột Giá Khởi Điểm
        javafx.scene.control.TableColumn<Auction, String> colPrice = new javafx.scene.control.TableColumn<>("Giá Khởi Điểm");
        colPrice.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getCurrentPrice();
            return new javafx.beans.property.SimpleStringProperty(String.format("%.2f", price));
        });
        colPrice.setPrefWidth(150);

        // Tạo cột Trạng Thái
        javafx.scene.control.TableColumn<Auction, String> colStatus = new javafx.scene.control.TableColumn<>("Trạng Thái");
        colStatus.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus() != null ?
                    cellData.getValue().getStatus().toString() : "N/A";
            return new javafx.beans.property.SimpleStringProperty(status);
        });
        colStatus.setPrefWidth(150);

        // Thêm các cột vào bảng
        tablePendingAuctions.getColumns().addAll(colId, colName, colPrice, colStatus);
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
    @FXML
    private void handleLogout() {
        com.auction.client.utils.UserSession.cleanUserSession();

        // Đóng tất cả cửa sổ hiện tại
        javafx.stage.Stage stage = (javafx.stage.Stage) javafx.stage.Stage.getWindows().stream()
                .filter(window -> window.isShowing())
                .findFirst()
                .orElse(null);

        if (stage != null) {
            stage.close();
        }

        // Mở lại Login
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/Login.fxml"));
                Parent root = loader.load();
                Stage loginStage = new Stage();
                loginStage.setTitle("Đăng Nhập");
                loginStage.setScene(new Scene(root));
                loginStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    @FXML private TableView<Auction> tablePendingAuctions;
    @FXML private Button btnApprove;

    @FXML
    private void handleApprove() {
        Auction selected = tablePendingAuctions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn một phiên đấu giá để duyệt!");
            return;
        }
        System.out.println("[Admin] Duyệt auction ID: " + selected.getId());
        networkService.approveAuction(selected.getId());
    }
}
