package com.auction.observer;

import com.auction.model.Bid;
import com.auction.observer.BidObserver;

public class BidLogger implements BidObserver {
    @Override
    public void update(String message) {
        System.out.println(message);
    }
}