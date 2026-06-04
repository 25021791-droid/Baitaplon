package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import com.auction.common.model.Auction;
import com.auction.common.model.User;
import com.auction.client.utils.UserSession;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private TableView<User> tableUsers;
    @FXML private TableView<Auction> tableAllAuctions;
    @FXML private TableView<Auction> tablePendingAuctions;

    @FXML private Button btnCancelAuction;
    @FXML private Button btnApprove;
    @FXML private Button btnRefresh;

    private NetworkClientService networkService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.networkService = NetworkClientService.getInstance();

        
        setupPendingAuctionsTable();
        setupAllAuctionsTable();

        
        btnRefresh.setOnAction(event -> loadSystemData());

        
        networkService.setOnActiveAuctionsReceived(auctions -> {
            Platform.runLater(() -> {
                tableAllAuctions.getItems().clear();
                tableAllAuctions.getItems().addAll(auctions);
                System.out.println("[Admin] Đã tải " + auctions.size() + " phiên đấu giá vào bảng quản lý chung.");
            });
        });

        
        networkService.setOnPendingAuctionsReceived(auctions -> {
            System.out.println("[Admin Client] Tiếp nhận danh sách chờ duyệt thành công. Số lượng: " + auctions.size());
            Platform.runLater(() -> {
                tablePendingAuctions.getItems().clear();
                tablePendingAuctions.getItems().addAll(auctions);
                System.out.println("[Admin Client] Đã cập nhật dữ liệu lên TableView chờ duyệt thành công.");
            });
        });

        
        networkService.setOnApproveAuctionResult(success -> {
            Platform.runLater(() -> {
                if (success) {
                    showAlert("Thành công", "Đã duyệt và kích hoạt phiên đấu giá này lên hệ thống công khai!");
                    networkService.requestPendingAuctions(); 
                    networkService.requestActiveAuctions();  
                } else {
                    showAlert("Lỗi", "Quá trình phê duyệt gặp sự cố. Vui lòng kiểm tra lại trạng thái DB!");
                }
            });
        });

        
        networkService.setOnCancelAuctionResult(success -> {
            Platform.runLater(() -> {
                if (success) {
                    showAlert("Thành công", "Hệ thống đã thu hồi và hủy bỏ phiên đấu giá được chọn.");
                } else {
                    showAlert("Lỗi", "Không thể thực hiện lệnh hủy phiên đấu giá này!");
                }
                
                loadSystemData();
            });
        });

        
        btnCancelAuction.setOnAction(event -> handleCancelSelectedAuction());
        btnApprove.setOnAction(event -> handleApprove());

        
        loadSystemData();
    }

    private void setupPendingAuctionsTable() {
        tablePendingAuctions.getColumns().clear();

        TableColumn<Auction, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> {
            Long id = cellData.getValue().getId();
            return new SimpleStringProperty(id != null ? id.toString() : "N/A");
        });
        colId.setPrefWidth(75);

        TableColumn<Auction, String> colName = new TableColumn<>("Tên Sản Phẩm");
        colName.setCellValueFactory(cellData -> {
            String name = cellData.getValue().getItem().getName();
            return new SimpleStringProperty(name != null ? name : "N/A");
        });
        colName.setPrefWidth(250);

        TableColumn<Auction, String> colPrice = new TableColumn<>("Giá Khởi Điểm");
        colPrice.setCellValueFactory(cellData -> {
            double price = cellData.getValue().getCurrentPrice();
            return new SimpleStringProperty(String.format("%.2f", price));
        });
        colPrice.setPrefWidth(150);

        TableColumn<Auction, String> colStatus = new TableColumn<>("Trạng Thái");
        colStatus.setCellValueFactory(cellData -> {
            String status = cellData.getValue().getStatus() != null ? cellData.getValue().getStatus().toString() : "N/A";
            return new SimpleStringProperty(status);
        });
        colStatus.setPrefWidth(150);

        tablePendingAuctions.getColumns().addAll(colId, colName, colPrice, colStatus);
    }

    private void setupAllAuctionsTable() {
        tableAllAuctions.getColumns().clear();

        TableColumn<Auction, String> colId = new TableColumn<>("ID Phiên");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        colId.setPrefWidth(75);

        TableColumn<Auction, String> colName = new TableColumn<>("Tên Sản Phẩm");
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItem().getName()));
        colName.setPrefWidth(250);

        TableColumn<Auction, String> colPrice = new TableColumn<>("Giá Cao Nhất");
        colPrice.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%.2f", cell.getValue().getCurrentPrice())));
        colPrice.setPrefWidth(150);

        TableColumn<Auction, String> colBidder = new TableColumn<>("Người Giữ Giá");
        colBidder.setCellValueFactory(cell -> {
            if (cell.getValue().getBids() != null && !cell.getValue().getBids().isEmpty()) {
                int lastIndex = cell.getValue().getBids().size() - 1;
                return new SimpleStringProperty(cell.getValue().getBids().get(lastIndex).getBidder().getName());
            }
            return new SimpleStringProperty("Chưa có");
        });
        colBidder.setPrefWidth(150);

        TableColumn<Auction, String> colStatus = new TableColumn<>("Trạng Thái");
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().toString()));
        colStatus.setPrefWidth(150);

        tableAllAuctions.getColumns().addAll(colId, colName, colPrice, colBidder, colStatus);
    }

    private void loadSystemData() {
        System.out.println("Admin đang phát tín hiệu yêu cầu đồng bộ toàn bộ dữ liệu từ Server...");
        networkService.requestActiveAuctions();
        networkService.requestPendingAuctions();
    }

    private void handleCancelSelectedAuction() {
        Auction selectedAuction = tableAllAuctions.getSelectionModel().getSelectedItem();

        if (selectedAuction == null) {
            showAlert("Thông báo", "Vui lòng chọn một phiên đấu giá trên bảng để tiến hành hủy bỏ!");
            return;
        }

        System.out.println("Admin yêu cầu hủy phiên: " + selectedAuction.getId());
        networkService.cancelAuction(selectedAuction.getId());
        showAlert("Đã gửi lệnh", "Yêu cầu hủy phiên số " + selectedAuction.getId() + " đã được đẩy lên hàng đợi của Server.");
    }

    @FXML
    private void handleApprove() {
        Auction selected = tablePendingAuctions.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng click chọn một phiên đấu giá trên bảng chờ duyệt trước!");
            return;
        }
        System.out.println("[Admin] Gửi lệnh duyệt phiên đấu giá có ID: " + selected.getId());
        networkService.approveAuction(selected.getId());
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/Profile.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.initOwner(btnRefresh.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Hồ sơ hệ thống - Admin");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Không thể khởi tạo hoặc nạp giao diện Profile.");
        }
    }

    @FXML
    private void handleLogout() {
        UserSession.cleanUserSession();

        
        Stage stage = (Stage) Stage.getWindows().stream()
                .filter(Window -> Window.isShowing())
                .findFirst()
                .orElse(null);

        if (stage != null) {
            stage.close();
        }

        
        Platform.runLater(() -> {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/Login.fxml"));
                Parent root = loader.load();
                Stage loginStage = new Stage();
                loginStage.setTitle("Hệ Thống Đấu Giá - Đăng Nhập");
                loginStage.setScene(new Scene(root));
                loginStage.show();
            } catch (Exception e) {
                System.err.println("[Lỗi] Không thể quay về màn hình Login: " + e.getMessage());
            }
        });
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