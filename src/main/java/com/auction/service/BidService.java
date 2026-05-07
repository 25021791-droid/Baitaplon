package com.auction.service;
import com.auction.model.*;
import com.auction.exception.*;
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
    public boolean placeBid(Auction auction, Bid bid)
            throws AuctionClosedException, InvalidBidException {

        lock.lock();

        try {
            if (auction.getStatus() != AuctionStatus.RUNNING) {
                throw new AuctionClosedException("Auction is closed. Cannot place bid.");
            }

            if (bid.getAmount() <= auction.getCurrentPrice()) {
                throw new InvalidBidException("Bid must be higher than current price.");
            }

            if (bid.getBidder().getBalance() < bid.getAmount()) {
                throw new InvalidBidException("Not enough money!");
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