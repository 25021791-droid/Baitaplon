package com.auction.common.model;

import java.time.LocalDateTime;

public class Bid {
    private Bidder bidder;
    private double amount;
    private LocalDateTime time;

    public Bid() {
        this.time = LocalDateTime.now();
    }

    public Bid(Bidder bidder, double amount) {
        this.bidder = bidder;
        this.amount = amount;
        this.time = LocalDateTime.now();
    }

    public Bid(Bidder bidder, double amount, LocalDateTime time) {
        this.bidder = bidder;
        this.amount = amount;
        this.time = time;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Bidder getBidder() {
        return bidder;
    }

    public void setBidder(Bidder bidder) {
        this.bidder = bidder;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getUsername() {
        return (bidder != null) ? bidder.getName() : "Ẩn danh";
    }
}