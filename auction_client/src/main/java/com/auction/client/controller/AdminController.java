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
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdminController implements Initializable {

    @FXML private TableView<String[]> tableUsers;
    @FXML private TableView<Auction> tableAllAuctions;
    @FXML private TableView<Auction> tablePendingAuctions;

    @FXML private Button btnCancelAuction;
    @FXML private Button btnApprove;
    @FXML private Button btnRefresh;
    @FXML private Button btnEditUser;
    @FXML private Button btnDeleteUser;
    @FXML private Label lblUsername;
    @FXML private Label lblAvatarLetter;

    private NetworkClientService networkService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.networkService = NetworkClientService.getInstance();

        User adminUser = UserSession.getUser();
        if (adminUser != null) {
            if (lblUsername != null) {
                lblUsername.setText("Admin: " + adminUser.getName());
            }
            if (lblAvatarLetter != null && adminUser.getName() != null && !adminUser.getName().isEmpty()) {
                lblAvatarLetter.setText(adminUser.getName().substring(0, 1).toUpperCase());
            }
        }

        setupUsersTable();
        setupPendingAuctionsTable();
        setupAllAuctionsTable();

        btnRefresh.setOnAction(event -> loadSystemData());

        networkService.setOnAllUsersReceived(users -> {
            Platform.runLater(() -> {
                tableUsers.getItems().clear();
                tableUsers.getItems().addAll(users);
                System.out.println("[Admin] Đã tải " + users.size() + " người dùng.");
            });
        });

        networkService.setOnDeleteUserResult(success -> {
            Platform.runLater(() -> {
                if (success) {
                    showAlert("Thành công", "Đã xóa người dùng thành công!");
                    networkService.requestAllUsers();
                } else {
                    showAlert("Lỗi", "Không thể xóa người dùng này!");
                }
            });
        });

        networkService.setOnUpdateUserResult(success -> {
            Platform.runLater(() -> {
                if (success) {
                    showAlert("Thành công", "Đã cập nhật thông tin người dùng!");
                    networkService.requestAllUsers();
                } else {
                    showAlert("Lỗi", "Không thể cập nhật thông tin người dùng!");
                }
            });
        });

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
        if (btnEditUser != null) btnEditUser.setOnAction(event -> handleEditUser());
        if (btnDeleteUser != null) btnDeleteUser.setOnAction(event -> handleDeleteUser());

        loadSystemData();
    }

    private void setupUsersTable() {
        tableUsers.getColumns().clear();

        TableColumn<String[], String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue()[0]));
        colId.setPrefWidth(60);

        TableColumn<String[], String> colUsername = new TableColumn<>("Tên Đăng Nhập");
        colUsername.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue()[1]));
        colUsername.setPrefWidth(150);

        TableColumn<String[], String> colEmail = new TableColumn<>("Email");
        colEmail.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue()[2]));
        colEmail.setPrefWidth(200);

        TableColumn<String[], String> colRole = new TableColumn<>("Vai Trò");
        colRole.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue()[3]));
        colRole.setPrefWidth(100);

        TableColumn<String[], String> colBalance = new TableColumn<>("Số Dư (VNĐ)");
        colBalance.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue()[4]));
        colBalance.setPrefWidth(120);

        tableUsers.getColumns().addAll(colId, colUsername, colEmail, colRole, colBalance);
    }

    private void setupPendingAuctionsTable() {
        tablePendingAuctions.getColumns().clear();

        TableColumn<Auction, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cellData -> {
            int id = cellData.getValue().getId();
            return new SimpleStringProperty(String.valueOf(id));
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
            return new SimpleStringProperty(String.format("%.0f VNĐ", price));
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
        colPrice.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%.0f VNĐ", cell.getValue().getCurrentPrice())));
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
        networkService.requestAllUsers();
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

    private void handleEditUser() {
        String[] selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn một người dùng để sửa!");
            return;
        }

        // Dialog sửa thông tin
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle("Sửa thông tin người dùng");
        dialog.setHeaderText("Chỉnh sửa người dùng ID: " + selected[0]);

        ButtonType btnSave = new ButtonType("Lưu", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(btnSave, ButtonType.CANCEL);

        javafx.scene.layout.GridPane grid = new javafx.scene.layout.GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField txtUsername = new TextField(selected[1]);
        TextField txtEmail = new TextField(selected[2]);

        grid.add(new Label("Tên đăng nhập:"), 0, 0);
        grid.add(txtUsername, 1, 0);
        grid.add(new Label("Email:"), 0, 1);
        grid.add(txtEmail, 1, 1);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == btnSave) {
                return new String[]{txtUsername.getText().trim(), txtEmail.getText().trim()};
            }
            return null;
        });

        Optional<String[]> result = dialog.showAndWait();
        result.ifPresent(data -> {
            if (data[0].isEmpty() || data[1].isEmpty()) {
                showAlert("Lỗi", "Tên đăng nhập và Email không được để trống!");
                return;
            }
            if (data[0].contains(",") || data[1].contains(",")) {
                showAlert("Lỗi", "Tên đăng nhập và Email không được chứa dấu phẩy!");
                return;
            }
            int userId = Integer.parseInt(selected[0]);
            networkService.updateUser(userId, data[0], data[1]);
        });
    }

    private void handleDeleteUser() {
        String[] selected = tableUsers.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Thông báo", "Vui lòng chọn một người dùng để xóa!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Bạn có chắc chắn muốn xóa người dùng này?");
        confirm.setContentText("Người dùng: " + selected[1] + " (ID: " + selected[0] + ")");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            int userId = Integer.parseInt(selected[0]);
            networkService.deleteUser(userId);
        }
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
            showAlert("Lỗi", "Không thể khởi tạo hoặc nạp giao diện Hồ sơ.");
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
                System.err.println("[Lỗi] Không thể quay về màn hình Đăng nhập: " + e.getMessage());
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