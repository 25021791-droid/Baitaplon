package com.auction.model;
import java.time.LocalDateTime;

public class Bid {
    private Bidder bidder;
    private double amount;
    private LocalDateTime time;

    public Bid(Bidder bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    public double getAmount() {
        return amount;
    }

    public Bidder getBidder() {
        return bidder;
    }
}