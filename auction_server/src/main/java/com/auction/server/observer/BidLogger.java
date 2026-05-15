package com.auction.server.observer;

import com.auction.common.model.Bid;
import com.auction.common.observer.BidObserver;

public class BidLogger implements BidObserver {
    @Override
    public void update(String message) {
        System.out.println(message);
    }
}