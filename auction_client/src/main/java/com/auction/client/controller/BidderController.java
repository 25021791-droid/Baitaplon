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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.scene.Parent;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.util.Base64;

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
    private Timeline timeline;
    
    private ObservableList<Bid> bidHistoryList = FXCollections.observableArrayList();
    private final List<BidderCardController> activeControllers = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resource) {
        this.networkService = NetworkClientService.getInstance();
        setupUser();
        setupBidLogTable();

        networkService.setOnActiveAuctionsReceived(auctionList -> {
            updateCardGrid(auctionList);
        });

        networkService.setOnEndedAuctionsReceived(auctions -> {
            updateEndedCards(auctions);
        });

        networkService.setOnBidResult(isSuccess -> {
            Platform.runLater(() -> {
                if (isSuccess) {
                    resultLabel.setText("Đặt giá đấu thành công!");
                    resultLabel.setStyle("-fx-text-fill: green;");
                    
                    double bidAmount = Double.parseDouble(bidField.getText());
                    Bid selfBid = new Bid(bidder, bidAmount);
                    bidHistoryList.add(0, selfBid); 

                    bidField.clear();
                    
                    networkService.requestActiveAuctions();
                } else {
                    resultLabel.setText("Đặt giá thất bại! Vui lòng thử lại.");
                    resultLabel.setStyle("-fx-text-fill: red;");
                }
            });
        });

        networkService.requestEndedAuctions();
        networkService.requestActiveAuctions();
    }

    private void updateCardGrid(List<Auction> auctionList) {
        javafx.application.Platform.runLater(() -> {
            flowActiveAuctions.getChildren().clear();
            flowEndedAuctions.getChildren().clear();

            for (BidderCardController controller : activeControllers) {
                controller.shutdown();
            }
            activeControllers.clear();

            for (Auction auction : auctionList) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/BidderCard.fxml"));
                    javafx.scene.Parent cardNode = loader.load();

                    BidderCardController controller = loader.getController();
                    controller.setData(auction);
                    activeControllers.add(controller);

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

            
            if (currentAuction != null) {
                for (Auction a : auctionList) {
                    if (a.getId() == currentAuction.getId()) {
                        this.currentAuction = a;
                        lblCurrentPrice.setText("Giá hiện tại: $" + a.getCurrentPrice());
                        break;
                    }
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

    private void updateEndedCards(List<Auction> auctions) {
        Platform.runLater(() -> {
            flowEndedAuctions.getChildren().clear();
            if (auctions.isEmpty()) {
                flowEndedAuctions.getChildren().add(lblNoEnded);
            } else {
                for (Auction auction : auctions) {
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/auction/BidderCard.fxml"));
                        Parent cardNode = loader.load();
                        BidderCardController cardController = loader.getController();
                        cardController.setData(auction);
                        flowEndedAuctions.getChildren().add(cardNode);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void updateAuctionDetails(Auction auction) {
        lblCurrentPrice.setText("Giá hiện tại: $" + auction.getCurrentPrice());
        lblItemDescription.setText("Mô tả: " + auction.getItem().getName() + "\nTình trạng: Hoạt động tốt.");

        
        String base64Image = auction.getItem().getImagePath();
        if (base64Image != null && !base64Image.isEmpty() && !"NO_IMAGE".equals(base64Image)) {
            try {
                byte[] imageBytes = Base64.getDecoder().decode(base64Image);
                Image img = new Image(new ByteArrayInputStream(imageBytes));
                itemImageView.setImage(img);
            } catch (Exception e) {
                System.err.println("[Lỗi] Không thể render ảnh Base64, nạp ảnh mặc định.");
                loadDefaultImage();
            }
        } else {
            loadDefaultImage();
        }

        setupTimer();
        if(bidField.isDisable()) {
            bidField.setDisable(false);
        }
        resultLabel.setText(""); 
    }

    private void loadDefaultImage() {
        try {
            Image image = new Image(getClass().getResourceAsStream("/images/default_item.png"));
            itemImageView.setImage(image);
        } catch (Exception e) {
            System.out.println("Không tìm thấy ảnh sản phẩm mặc định.");
        }
    }

    private void setupBidLogTable() {
        colUser.setCellValueFactory(new PropertyValueFactory<>("username"));
        colAmount.setCellValueFactory(new PropertyValueFactory<>("amount"));
        colTime.setCellValueFactory(new PropertyValueFactory<>("time"));

        bidTable.setItems(bidHistoryList);
    }

    private void setupTimer() {
        if (timeline != null) {
            timeline.stop();
        }

        timeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> {
            java.time.Duration duration = java.time.Duration.between(LocalDateTime.now(), currentAuction.getEndTime());

            if (duration.isNegative() || duration.isZero()) {
                lblTimeLeft.setText("Phiên đấu giá đã kết thúc!");
                lblTimeLeft.setStyle("-fx-text-fill: red;");
                timeline.stop();
            } else {
                long hours = duration.toHours();
                long minutes = duration.toMinutes() % 60;
                long seconds = duration.getSeconds() % 60;


                String timeString = "";
                if (hours > 0) {
                    timeString += hours + "h ";
                }
                timeString += String.format("%02dm %02ds", minutes, seconds);

                lblTimeLeft.setText(timeString);
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
    private void handleProfile() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/auction/Profile.fxml"));
            javafx.scene.Parent root = loader.load();
            javafx.stage.Stage dialog = new javafx.stage.Stage();
            dialog.initOwner(lblUsername.getScene().getWindow());
            dialog.initModality(javafx.stage.Modality.WINDOW_MODAL);
            dialog.setTitle("Profile");
            dialog.setScene(new javafx.scene.Scene(root));
            dialog.showAndWait();
            setupUser(); 
        } catch (java.io.IOException e) {
            e.printStackTrace();
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

    @FXML
    private void handleRefresh() {
        networkService.requestActiveAuctions();
        networkService.requestEndedAuctions();
    }
}