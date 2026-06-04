package com.auction.client.observer;

import com.auction.common.model.Bid;
import com.auction.common.observer.BidObserver;
import javafx.application.Platform;
import javafx.scene.control.Label;

public class BidUIObserver implements BidObserver {

    private final Label label;

    public BidUIObserver(Label label) {
        this.label = label;
    }

    @Override
    public void update(String message) {
        Platform.runLater(() -> {
            label.setText(message);
        });
    }

    
    @Override
    public void update(Bid newBid) {
        Platform.runLater(() -> {
            if (newBid != null && newBid.getBidder() != null) {
                
                String uiMessage = String.format("%s vừa cược: %.0f VNĐ",
                        newBid.getBidder().getName(),
                        newBid.getAmount());
                label.setText(uiMessage);
            }
        });
    }
}