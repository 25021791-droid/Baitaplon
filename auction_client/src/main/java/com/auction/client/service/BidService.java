package com.auction.client.service;

import com.auction.common.model.Auction;
import com.auction.common.model.Bid;
import com.auction.common.observer.BidObserver;

import java.util.ArrayList;
import java.util.List;

public class BidService {

    private final List<BidObserver> observers = new ArrayList<>();

    public void addObserver(BidObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(String message) {
        for (BidObserver observer : observers) {
            observer.update(message);
        }
    }
}
