package main.java.com.auction.observer;

import main.java.com.auction.model.Bid;

public interface BidObserver {
    void update(Bid bid);
}