package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import com.auction.common.model.Auction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TableView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;
import javafx.fxml.Initializable;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import java.io.File;
import java.io.IOException;
import com.auction.common.model.User;
import com.auction.client.utils.UserSession;
import com.auction.common.model.User;
public class SellerController implements Initializable {

    @FXML private TextField txtItemName;
    @FXML private TextField txtStartPrice;
    @FXML private Button btnCreateAuction;
    @FXML private Button btnChooseImage;
    @FXML private Label lblImageName;
    @FXML private ImageView imgPreview;

    private File selectedImageFile;
    private String imageBase64;
    // Bảng để Seller xem các mặt hàng mình đang bán
    @FXML private TableView<Auction> tableMyAuctions;

    private NetworkClientService networkService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        this.networkService = NetworkClientService.getInstance();

        // Sự kiện khi bấm nút Tạo phiên đấu giá
        btnCreateAuction.setOnAction(event -> handleCreateAuction());
        btnChooseImage.setOnAction(event -> handleChooseImage());

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

            User currentUser = UserSession.getUser();
            if (currentUser == null) {
                showAlert("Lỗi", "Bạn chưa đăng nhập!");
                return;
            }
            int sellerId = currentUser.getId();

            // Đăng ký callback nhận kết quả
            networkService.setOnCreateAuctionResult(isSuccess -> {
                Platform.runLater(() -> {
                    if (isSuccess) {
                        showAlert("Thành công", "Phiên đấu giá đã được tạo!");
                        txtItemName.clear();
                        txtStartPrice.clear();
                        lblImageName.setText("Chưa chọn ảnh");
                        imgPreview.setVisible(false);
                        selectedImageFile = null;
                    } else {
                        showAlert("Thất bại", "Không thể tạo phiên đấu giá!");
                    }
                });
            });

            // Gửi request (không cần encode Base64 nữa!)
            networkService.createAuction(itemName, startPrice, sellerId, selectedImageFile);

        } catch (NumberFormatException e) {
            showAlert("Lỗi nhập liệu", "Giá khởi điểm phải là một con số hợp lệ!");
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
            showAlert("Error", "Cannot open profile screen.");
        }
    }
    @FXML
    private void handleLogout() {
        com.auction.client.utils.UserSession.cleanUserSession();
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
    private void handleChooseImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh sản phẩm");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        selectedImageFile = fileChooser.showOpenDialog(btnChooseImage.getScene().getWindow());

        if (selectedImageFile != null) {
            lblImageName.setText(selectedImageFile.getName());

            // Hiển thị preview
            Image image = new Image(selectedImageFile.toURI().toString());
            imgPreview.setImage(image);
            imgPreview.setVisible(true);

            // KHÔNG CẦN encodeBase64 nữa!
        }
    }
}
