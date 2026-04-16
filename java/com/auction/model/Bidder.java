package com.auction.model;

import java.util.ArrayList;
import java.util.List;

public class Bidder extends User {
    private double balance;
    private List<Bid> bidHistory;

    public Bidder(int userid, String userName, String password, String email, double balance) {
        super(userId, userName, password, email);
        this.balance = balance;
        this.bidHistory = new ArrayList<>();
    }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    @Override
    public void displayDashboard() {
        System.out.println("Bidder Dashboard");
    }
    public boolean placeBid(Auction currentPrice, double price) {
        if (price > this.balance) {
            System.out.println("Error");
            return false;
        }
        if (price <= currentPrice.getCurrentPrice()) {
            System.out.println("Error");
            return false;
        }
        Bid newBid = new Bid(this, price);
        this.bidHistory.add(newBid);
        item.addBid(newBid);
        return true;
    }
    public void registerAutoBid(Item item, double maxBid, double increment) {

    }
}
