package com.auction.client.controller;

import com.auction.client.service.AuctionClientService;
import com.auction.common.model.*;
import com.auction.client.utils.UserSession;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;

import java.net.URL;
import java.util.ResourceBundle;


public class AuctionController implements Initializable {

    @FXML private TextField bidField;
    @FXML private Label resultLabel;
    @FXML private Label lblUsername;

    private Bidder bidder;
    private Auction auction;

    @Override
    public void initialize(URL location, ResourceBundle resource) {
        User user = UserSession.getUser();

        if (user != null) {
            lblUsername.setText("User: " + user.getName());
            if (user instanceof Bidder) {
                this.bidder = (Bidder) user;
            }
        } else {
            this.bidder = new Bidder(1, "dat", "a@gmail.com", 1000.0);
            lblUsername.setText("User: " + bidder.getName());
        }

        Item item = new Electronics(1, "Laptop");
        this.auction = new Auction(item, 100);

        // bidService.addObserver(new BidLogger());
        // bidService.addObserver(new BidUIObserver(resultLabel));
        // -- chuyển hết service qua server side r
    }

    @FXML
    private void handleBid() {
        try {
            double amount = Double.parseDouble(bidField.getText());
            if (amount > bidder.getBalance()) {
                resultLabel.setText("Not enough money!");
                return;
            }

            boolean sendOk = AuctionClientService.getInstance().placeBid(auction.getId(), bidder.getId(), amount);

            if (sendOk) {
                resultLabel.setText("Bid placed successfully...");
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
            UserSession.cleanUserSession();
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
    }
}