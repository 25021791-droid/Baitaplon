package com.auction.common.model;

import java.util.ArrayList;
import java.util.List;

public class Auction {

    private Long id;
    private Item item;
    private List<Bid> bids;
    private double currentPrice;
    private AuctionStatus status;

    public Auction(Item item, double startPrice) {
        this.item = item;
        this.currentPrice = startPrice;
        this.bids = new ArrayList<>();
        this.status = AuctionStatus.OPEN;
    }

    // Getters and Setters

    public Long getId() {
        return (long)100;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public List<Bid> getBids() {
        return bids;
    }

    public void setBids(List<Bid> bids) {
        this.bids = bids;
    }

    public double getCurrentPrice() {
        return currentPrice;
    }

    public void setCurrentPrice(double price) {
        this.currentPrice = price;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }
}