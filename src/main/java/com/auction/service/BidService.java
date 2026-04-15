package com.auction.service;
import com.auction.model.*;
import java.util.*;
import com.auction.observer.*;
import java.util.concurrent.locks.*;
public class BidService {
    private Lock lock = new ReentrantLock();
    private List<BidObserver> observers = new ArrayList<>();
    public void addObserver(BidObserver o) {
        observers.add(o);
    }
    public void removeObserver(BidObserver o) {
        observers.remove(o);
    }
    private void notifyObservers(Bid bid) {
        for (BidObserver o : observers) {
            o.update(bid);
        }
    }
    public boolean placeBid(Auction auction, Bid bid) {
        lock.lock();
        try {
            if (auction.getStatus() != AuctionStatus.OPEN) {
                System.out.println("Auction not open!");
                return false;
            }

            if (bid.getAmount() <= auction.getCurrentPrice()) {
                System.out.println("Bid must be higher!");
                return false;
            }

            if (bid.getBidder().getBalance() < bid.getAmount()) {
                System.out.println("Not enough money!");
                return false;
            }

            auction.getBids().add(bid);
            auction.setCurrentPrice(bid.getAmount());
            notifyObservers(bid);

            return true;

        } finally {
            lock.unlock();
        }
    }


    public Bid getWinner(Auction auction) {
        List<Bid> bids = auction.getBids();
        if (bids.isEmpty()) return null;
        return bids.get(bids.size() - 1);
    }

    public void printBids(Auction auction) {
        for (Bid b : auction.getBids()) {
            System.out.println(
                    b.getBidder().getName() + " : " + b.getAmount()
            );
        }
    }

}