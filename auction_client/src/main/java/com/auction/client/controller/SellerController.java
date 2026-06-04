package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import com.auction.client.utils.UserSession;
import com.auction.common.model.Auction;
import com.auction.common.model.User;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class SellerController implements Initializable {

    @FXML private TextField txtItemName;
    @FXML private TextField txtStartPrice;
    @FXML private Button btnCreateAuction;
    @FXML private Button btnChooseImage;
    @FXML private Label lblImageName;
    @FXML private ImageView imgPreview;
    @FXML private TextField txtDuration;
    @FXML private TableView<Auction> tableMyAuctions;
    @FXML private Label lblUsername;
    @FXML private Label lblAvatarLetter;

    private File selectedImageFile;
    private NetworkClientService networkService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.networkService = NetworkClientService.getInstance();

        setupMyAuctionsTable();

        User currentUser = UserSession.getUser();
        if (currentUser != null) {
            networkService.requestMyAuctions(currentUser.getId());
            if (lblUsername != null) {
                lblUsername.setText("Seller: " + currentUser.getName());
            }
            if (lblAvatarLetter != null && currentUser.getName() != null && !currentUser.getName().isEmpty()) {
                lblAvatarLetter.setText(currentUser.getName().substring(0, 1).toUpperCase());
            }
        }

        networkService.setOnMyAuctionsReceived(auctions -> {
            Platform.runLater(() -> {
                tableMyAuctions.getItems().clear();
                tableMyAuctions.getItems().addAll(auctions);
                System.out.println("[Seller] Đã load " + auctions.size() + " auction");
            });
        });

        networkService.setOnCreateAuctionResult(isSuccess -> {
            Platform.runLater(() -> {
                if (isSuccess) {
                    showAlert("Thành công", "Phiên đấu giá đã được tạo!");
                    txtItemName.clear();
                    txtStartPrice.clear();
                    lblImageName.setText("Chưa chọn ảnh");
                    imgPreview.setVisible(false);
                    txtDuration.clear();
                    selectedImageFile = null;
                    User user = UserSession.getUser();
                    if (user != null) {
                        networkService.requestMyAuctions(user.getId());
                    }
                } else {
                    showAlert("Thất bại", "Không thể tạo phiên đấu giá!");
                }
            });
        });

        btnCreateAuction.setOnAction(event -> handleCreateAuction());
        btnChooseImage.setOnAction(event -> handleChooseImage());
    }

    private void setupMyAuctionsTable() {
        tableMyAuctions.getColumns().clear();

        TableColumn<Auction, String> colId = new TableColumn<>("ID");
        colId.setCellValueFactory(cell -> new SimpleStringProperty(String.valueOf(cell.getValue().getId())));
        colId.setPrefWidth(75);

        TableColumn<Auction, String> colName = new TableColumn<>("Tên Sản Phẩm");
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getItem().getName()));
        colName.setPrefWidth(250);

        TableColumn<Auction, String> colPrice = new TableColumn<>("Giá Hiện Tại");
        colPrice.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%.0f VNĐ", cell.getValue().getCurrentPrice())));
        colPrice.setPrefWidth(150);

        TableColumn<Auction, String> colStatus = new TableColumn<>("Trạng Thái");
        colStatus.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getStatus().toString()));
        colStatus.setPrefWidth(150);

        tableMyAuctions.getColumns().addAll(colId, colName, colPrice, colStatus);
    }

    private void handleCreateAuction() {
        String itemName = txtItemName.getText().trim();
        String priceStr = txtStartPrice.getText().trim();

        if (itemName.isEmpty() || priceStr.isEmpty()) {
            showAlert("Lỗi nhập liệu", "Vui lòng nhập đầy đủ tên sản phẩm và giá khởi điểm!");
            return;
        }

        if (itemName.contains(",")) {
            showAlert("Lỗi nhập liệu", "Tên sản phẩm không được chứa dấu phẩy!");
            return;
        }

        try {
            double startPrice = Double.parseDouble(priceStr);
            if (startPrice <= 0) {
                showAlert("Lỗi giá", "Giá khởi điểm phải lớn hơn 0!");
                return;
            }

            User currentUser = UserSession.getUser();
            if (currentUser == null) {
                showAlert("Lỗi", "Bạn chưa đăng nhập!");
                return;
            }
            int sellerId = currentUser.getId();

            int durationMinutes = 4320;
            String durationStr = txtDuration.getText().trim();
            if (!durationStr.isEmpty()) {
                try {
                    durationMinutes = Integer.parseInt(durationStr);
                    if (durationMinutes <= 0 || durationMinutes > 4320) {
                        showAlert("Lỗi thời gian", "Thời gian đấu giá phải từ 1 đến 4320 phút (tối đa 3 ngày)!");
                        return;
                    }
                } catch (NumberFormatException ex) {
                    showAlert("Lỗi nhập liệu", "Thời gian đấu giá phải là số nguyên!");
                    return;
                }
            }
            networkService.createAuction(itemName, startPrice, sellerId, selectedImageFile, durationMinutes);

        } catch (NumberFormatException e) {
            showAlert("Lỗi nhập liệu", "Giá khởi điểm phải là một con số hợp lệ!");
        }
    }

    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        selectedImageFile = fileChooser.showOpenDialog(btnChooseImage.getScene().getWindow());

        if (selectedImageFile != null) {
            lblImageName.setText(selectedImageFile.getName());
            Image image = new Image(selectedImageFile.toURI().toString());
            imgPreview.setImage(image);
            imgPreview.setVisible(true);
        }
    }

    @FXML
    private void handleProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/Profile.fxml"));
            Parent root = loader.load();
            Stage dialog = new Stage();
            dialog.initOwner(btnCreateAuction.getScene().getWindow());
            dialog.initModality(Modality.WINDOW_MODAL);
            dialog.setTitle("Profile");
            dialog.setScene(new Scene(root));
            dialog.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLogout() {
        UserSession.cleanUserSession();
        Stage stage = (Stage) btnCreateAuction.getScene().getWindow();
        stage.close();
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