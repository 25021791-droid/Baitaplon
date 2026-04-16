package com.auction.observer;

import com.auction.model.Bid;

public interface BidObserver {
    void update(Bid bid);
}