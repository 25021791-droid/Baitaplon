package com.auction.common.model;

import java.util.ArrayList;
import java.util.List;

public class Bidder extends User {
    private double balance;
    private List<Bid> bidHistory;

    
    public Bidder(int userid, String username, String email, double balance) {
        super(userid, username, email, "BIDDER");
        this.balance = balance;
        this.bidHistory = new ArrayList<>();
    }

    public Bidder(int userid, String username, String email) {
        this(userid, username, email, 100000.0);
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    @Override
    public void displayDashboard() {
        System.out.println("Bidder Dashboard");
    }

    public boolean placeBid(Auction auction, double price) {
        if (price > this.balance) {
            System.out.println("[Từ chối] Số dư trong ví không đủ để thực hiện mức cược này.");
            return false;
        }
        if (price <= auction.getCurrentPrice()) {
            System.out.println("[Từ chối] Mức giá đặt phải cao hơn giá cao nhất hiện tại.");
            return false;
        }

        
        Bid newBid = new Bid(this, price);
        this.bidHistory.add(newBid);

        
        auction.getBids().add(newBid);

        
        auction.setCurrentPrice(price);

        System.out.println("[Thành công] Lượt cược của " + this.getName() + " đã được ghi nhận. Giá mới: " + price);
        return true;
    }

    public void registerAutoBid(Item item, double maxBid, double increment) {
        
        System.out.println("Tính năng đấu giá tự động (AutoBid) đã được kích hoạt cho: " + item.getName());
    }
}