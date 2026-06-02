package com.auction.common.model;

import java.util.ArrayList;
import java.util.List;

public class Bidder extends User {
    private double balance;
    private List<Bid> bidHistory;

    // Constructor khi đã có số dư (từ database)
    public Bidder(int userid, String username, String email, double balance) {
        super(userid, username, email, "BIDDER");
        this.balance = balance;
        this.bidHistory = new ArrayList<>();
    }
    // Constructor khi tạo tk mới
    public Bidder(int userid, String username, String email) {
        this(userid, username, email, 100000.0);
    }

    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    @Override
    public void displayDashboard() {
        System.out.println("Bidder Dashboard");
    }
    public boolean placeBid(Auction currentPrice, double price, Item item) {
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
        System.out.println("Automatic pricing has been enabled for: " + item.getName());
    }
}
