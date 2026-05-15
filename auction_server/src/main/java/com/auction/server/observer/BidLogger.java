package com.auction.server.observer;

import com.auction.common.model.Bid;
import com.auction.common.observer.BidObserver;

public class BidLogger implements BidObserver {
    @Override
    public void update(Bid bid) {
        System.out.println("New bid: " + bid.getAmount());
    }
}