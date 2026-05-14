package com.auction.observer;

import com.auction.model.Bid;

public class BidLogger implements BidObserver {
    @Override
    public void update(String message) {
        System.out.println(message);
    }
}