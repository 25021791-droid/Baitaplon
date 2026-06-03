package com.auction.common.model;

import java.time.LocalDateTime;

public class Bid {
    private Bidder bidder;
    private double amount;
    private LocalDateTime time;

    public Bid() {
    }

    public Bid(Bidder bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    public double getAmount() {
        return amount;
    }
    public void setAmount(double amount) { this.amount = amount; }

    public Bidder getBidder() {
        return bidder;
    }
    public void setBidder(Bidder b) { this.bidder = b; }

    public LocalDateTime getBidTime() { return time; }
    public void setBidTime(LocalDateTime time) { this.time = time; }
}