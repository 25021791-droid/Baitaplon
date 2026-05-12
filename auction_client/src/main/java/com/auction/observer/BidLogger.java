package com.auction.observer;

import com.auction.model.Bid;
import com.auction.observer.BidObserver;

public class BidLogger implements BidObserver {
    @Override
    public void update(Bid bid) {
        System.out.println("New bid: " + bid.getAmount());
    }
}