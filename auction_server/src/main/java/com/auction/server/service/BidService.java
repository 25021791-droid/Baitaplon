package com.auction.server.service;

import com.auction.common.model.Auction;
import com.auction.common.model.Bid;
import com.auction.common.observer.BidObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

    public Bid getWinner(Auction auction) {
        if (auction == null || auction.getBids() == null || auction.getBids().isEmpty()) {
            return null; 
        }

        List<Bid> bids = auction.getBids();
        return bids.get(bids.size() - 1);
    }
}