package com.auction.common.observer;

import com.auction.common.model.Bid;

public interface BidObserver {

    void update(String message);

    void update(Bid newBid);
}