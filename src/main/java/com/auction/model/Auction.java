package com.auction.model;

import java.util.ArrayList;
import java.util.List;

public class Auction {
    private Item item;
    private List<Bid> bids;
    private double currentPrice;
    private AuctionStatus status;

    public Auction(Item item) {
        this.item = item;
        this.currentPrice = item.getStartingPrice();
        this.bids = new ArrayList<>();
        this.status = AuctionStatus.OPEN;
    }
    public void closeAuction() {
        status = AuctionStatus.FINISHED;
        if (!bids.isEmpty()) {
            Bid winner = bids.get(bids.size() - 1);
            System.out.println("Winner: " + winner.getBidder().getName());
        }
    }
    public List<Bid> getBids() { return bids; }
    public double getCurrentPrice() { return currentPrice; }
    public void setCurrentPrice(double price) { this.currentPrice = price; }
    public AuctionStatus getStatus() { return status; }
    public void setStatus(AuctionStatus status) { this.status = status; }
}
