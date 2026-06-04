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

    
    
    private final Lock bidLock = new ReentrantLock();

    public void addObserver(BidObserver observer) {
        observers.add(observer);
    }

    private void notifyObservers(String message) {
        for (BidObserver observer : observers) {
            observer.update(message);
        }
    }

    
    private void notifyObservers(Bid newBid) {
        for (BidObserver observer : observers) {
            
            
        }
    }

    public boolean placeBid(Auction auction, Bid bid) {
        bidLock.lock();

        try {
            
            if (auction.isEnded()) {
                notifyObservers("Lỗi: Phiên đấu giá " + auction.getId() + " đã đóng!");
                return false;
            }

            
            if (bid.getAmount() <= auction.getCurrentPrice()) {
                return false; 
            }

            
            auction.setCurrentPrice(bid.getAmount());
            auction.getBids().add(bid);

            
            notifyObservers(String.format("Phiên [%d] - Cập nhật giá mới: %,.0f VNĐ", auction.getId(), bid.getAmount()));

            return true;

        } catch (Exception e) {
            System.err.println("Lỗi xử lý cược: " + e.getMessage());
            return false;
        } finally {
            bidLock.unlock();
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