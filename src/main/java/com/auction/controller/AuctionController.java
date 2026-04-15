package com.auction.controller;

import com.auction.model.*;
import com.auction.service.*;
import com.auction.observer.*;
import com.auction.service.BidService;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class AuctionController {

    @FXML
    private TextField bidField;

    @FXML
    private Label resultLabel;

    private AuctionService service = new AuctionService();
    private BidService bidService = new BidService();

    private Bidder bidder;
    private Auction auction;


    @FXML
    public void initialize() {
        bidder = new Bidder(1, "dat", "123", "a@gmail.com", 1000);
        Item item = new Electronics(1, "Laptop");
        auction = new Auction(item,100);
        bidService.addObserver(new BidLogger());
        bidService.addObserver(new BidUIObserver(resultLabel));
    }
    @FXML
    private void handleBid() {
        try {
            double amount = Double.parseDouble(bidField.getText());

            boolean ok = bidService.placeBid(auction, new Bid(bidder, amount));

            if (!ok) {
                resultLabel.setText("Bid failed!");
            }

        } catch (Exception e) {
            resultLabel.setText("Invalid input!");
        }
    }

}