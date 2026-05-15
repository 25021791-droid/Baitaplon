package com.auction.client.observer;

import javafx.scene.control.Label;
import com.auction.common.model.Bid;
import com.auction.common.observer.BidObserver;

public class BidUIObserver implements BidObserver {

    private Label label;

    public BidUIObserver(Label label) {
        this.label = label;
    }

    @Override
    public void update(Bid bid) {
        label.setText("Latest bid: " + bid.getAmount());
    }
}