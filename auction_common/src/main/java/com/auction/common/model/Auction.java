package com.auction.common.model;

import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class Auction {
    private LocalDateTime endTime;

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    private Long id; // Added an ID field
    private Item item;
    private List<Bid> bids;
    private double currentPrice;
    private AuctionStatus status;
    private int sellerId;
    public int getSellerId() {
        return sellerId;
    }

    public void setSellerId(int sellerId) {
        this.sellerId = sellerId;
    }
    public Auction(Item item, double startPrice) {
        this.item = item;
        this.currentPrice = startPrice;
        this.bids = new ArrayList<>();
        this.status = AuctionStatus.OPEN;
    }

    public boolean isEnded() {
        return this.status == AuctionStatus.FINISHED ||
               this.status == AuctionStatus.CANCELED ||
               this.status == AuctionStatus.PAID;
    }
    public Long getId() {
        return id;
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