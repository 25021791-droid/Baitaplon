package com.auction.client.controller;

import com.auction.client.service.NetworkClientService;
import com.auction.common.model.*;
import com.auction.client.utils.UserSession;
import javafx.animation.*;
import javafx.collections.*;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.scene.layout.FlowPane;
import javafx.util.Duration;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class BidderController implements Initializable {

    @FXML private TextField bidField;
    @FXML private Label resultLabel;
    @FXML private Label lblUsername;
    @FXML private Label lblCurrentPrice;
    @FXML private Label lblBalance;
    @FXML private Label lblItemDescription;
    @FXML private Label lblTimeLeft;
    @FXML private ImageView itemImageView;
    @FXML private FlowPane flowActiveAuctions;
    @FXML private FlowPane flowEndedAuctions;
    @FXML private Label lblNoEnded;
    @FXML private TableView<Bid> bidTable;
    @FXML private TableColumn<Bid, String> colUser;
    @FXML private TableColumn<Bid, Double> colAmount;
    @FXML private TableColumn<Bid, String> colTime;

    private Bidder bidder;
    private Auction currentAuction;
    private NetworkClientService networkService;
    private int secondsRemaining;
    private Timeline timeline;

    @Override
    public void initialize(URL location, ResourceBundle resource) {
        this.networkService = NetworkClientService.getInstance();
        setupUser();
        setupBidLogTable();

        networkService.setOnActiveAuctionsReceived(auctionList -> {
            updateCardGrid(auctionList);
        });

        networkService.requestActiveAuctions();
    }

    // ĐÃ CẬP NHẬT: Đưa toàn bộ logic nạp thẻ từ Server vào đây
    private void updateCardGrid(List<Auction> auctionList) {
        javafx.application.Platform.runLater(() -> {
            flowActiveAuctions.getChildren().clear();
            flowEndedAuctions.getChildren().clear();

            for (Auction auction : auctionList) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/BidderCard.fxml"));
                    javafx.scene.Parent cardNode = loader.load();

                    BidderCardController cardController = loader.getController();
                    cardController.setData(auction);

                    cardNode.setOnMouseClicked(event -> {
                        this.currentAuction = auction;
                        updateAuctionDetails(auction);
                    });

                    if (auction.isEnded()) {
                        if (lblNoEnded != null) {
                            flowEndedAuctions.getChildren().remove(lblNoEnded);
                        }
                        flowEndedAuctions.getChildren().add(cardNode);
                    } else {
                        flowActiveAuctions.getChildren().add(cardNode);
                    }

                } catch (java.io.IOException e) {
                    System.err.println("Lỗi load file AuctionCard.fxml. Kiểm tra lại tên file!");
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupUser() {
        User user = UserSession.getUser();
        if (user instanceof Bidder) {
            this.bidder = (Bidder) user;
            lblUsername.setText("User: " + bidder.getName());
            lblBalance.setText(String.format("$%.2f", bidder.getBalance()));
        }
    }

    private void updateAuctionDetails(Auction auction) {
        lblCurrentPrice.setText("Giá hiện tại: $" + auction.getCurrentPrice());
        lblItemDescription.setText("Mô tả: " + auction.getItem().getName() + "\nTình trạng: Hoạt động tốt.");

        try {
            Image image = new Image(getClass().getResourceAsStream("/images/default_item.png"));
            itemImageView.setImage(image);
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh sản phẩm mặc định.");
        }

        this.secondsRemaining = 300;
        setupTimer();
    }

    private void setupBidLogTable() {
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        ObservableList<Bid> history = FXCollections.observableArrayList();
        bidTable.setItems(history);
    }

    private void setupTimer() {
        if (timeline != null) {
            timeline.stop();
        }
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            secondsRemaining--;
            if (secondsRemaining <= 0) {
                lblTimeLeft.setText("Phiên đấu giá đã kết thúc!");
                timeline.stop();
                bidField.setDisable(true);
            } else {
                int minutes = secondsRemaining / 60;
                int seconds = secondsRemaining % 60;
                lblTimeLeft.setText(String.format("Thời gian còn lại: %02d:%02d", minutes, seconds));
            }
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    @FXML
    private void handleBid() {
        if (currentAuction == null) {
            resultLabel.setText("Vui lòng chọn một sản phẩm để đấu giá!");
            resultLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            double amount = Double.parseDouble(bidField.getText());
            if (amount <= currentAuction.getCurrentPrice()) {
                resultLabel.setText("Giá bid phải lớn hơn giá hiện tại!");
                resultLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            if (amount > bidder.getBalance()) {
                resultLabel.setText("Bạn không đủ tiền trong ví!");
                resultLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            resultLabel.setText("Đang xử lý...");
            resultLabel.setStyle("-fx-text-fill: blue;");
            networkService.placeBid(currentAuction.getId(), bidder.getId(), amount);

        } catch (NumberFormatException e) {
            resultLabel.setText("Vui lòng nhập một số hợp lệ!");
            resultLabel.setStyle("-fx-text-fill: red;");
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            if (timeline != null) timeline.stop();
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