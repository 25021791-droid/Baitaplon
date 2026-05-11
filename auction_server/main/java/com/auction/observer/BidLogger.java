package main.java.com.auction.observer;

import main.java.com.auction.model.Bid;

public class BidLogger implements BidObserver {
    @Override
    public void update(Bid bid) {
        System.out.println("New bid: " + bid.getAmount());
    }
}