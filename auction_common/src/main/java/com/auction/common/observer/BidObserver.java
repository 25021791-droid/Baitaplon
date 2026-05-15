package com.auction.common.observer;

import com.auction.common.model.Bid;

public interface BidObserver {
    void update(Bid bid);
}