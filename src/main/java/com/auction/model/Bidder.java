package com.auction.model;

import java.util.ArrayList;
import java.util.List;

public class Bidder extends User {
    private double balance;
    private List<Bid> bidHistory;

    public Bidder(int id, String username, String password, String email, double balance) {
        super(id, username, password, email);
        this.balance = balance;
        this.bidHistory = new ArrayList<>();
    }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    @Override
    public void displayDashboard() {
        System.out.println("Bidder Dashboard");
    }
}