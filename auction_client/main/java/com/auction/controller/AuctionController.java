package com.auction.controller;

import com.auction.model.*;
import com.auction.service.*;
import com.auction.observer.*;
import com.auction.service.BidService;
import com.auction.utils.UserSession;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import java.net.URL;
import java.util.ResourceBundle;

public class AuctionController implements Initializable {

    @FXML private TextField bidField;
    @FXML private Label resultLabel;
    @FXML private Label lblUsername;
    @FXML private Label lblBalance;

    private AuctionService service = new AuctionService();
    private BidService bidService = new BidService();

    private Bidder bidder;
    private Auction auction;

    @Override
    public void initialize(URL location, ResourceBundle resource) {
        User user = UserSession.getUser();

        if (user != null) {
            lblUsername.setText("User: " + user.getName());
            if (user instanceof Bidder) {
                this.bidder = (Bidder) user;
                lblBalance.setText("Balance: " + bidder.getBalance());
            }
        } else {
            this.bidder = new Bidder(1, "dat", "a@gmail.com", 1000.0);
            lblUsername.setText("User: " + bidder.getName());
            lblBalance.setText("Balance: " + bidder.getBalance());
        }

        Item item = new Electronics(1, "Laptop");
        this.auction = new Auction(item, 100);

        bidService.addObserver(new BidLogger());
        bidService.addObserver(new BidUIObserver(resultLabel));
    }

    @FXML
    private void handleBid() {
        try {
            double amount = Double.parseDouble(bidField.getText());
            if (amount > bidder.getBalance()) {
                resultLabel.setText("Not enough money!");
                return;
            }

            boolean ok = bidService.placeBid(auction, new Bid(bidder, amount));

            if (ok) {
                lblBalance.setText("Balance: " + bidder.getBalance());
            } else {
                resultLabel.setText("Bid failed!");
            }

        } catch (NumberFormatException e) {
            resultLabel.setText("Please enter a number!");
        } catch (Exception e) {
            resultLabel.setText("Error occurred!");
            e.printStackTrace();
        }
    }

    @FXML
    private void handleBackToLogin() {
        try {
            javafx.scene.Parent root = javafx.fxml.FXMLLoader.load(getClass().getResource("/com/auction/Login.fxml"));
            javafx.stage.Stage stage = (javafx.stage.Stage) lblUsername.getScene().getWindow();
            stage.setScene(new javafx.scene.Scene(root));
            stage.show();
            com.auction.utils.UserSession.cleanUserSession();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}