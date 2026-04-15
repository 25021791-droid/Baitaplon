package com.auction.model;

public class Admin extends User {

    public Admin(int id, String username, String password, String email) {
        super(id, username, password, email);
    }

    public void manageUsers() {
        System.out.println("Managing users...");
    }

    public void manageAuctions() {
        System.out.println("Managing auctions...");
    }

    @Override
    public void displayDashboard() {
        System.out.println("Admin Dashboard");
    }
}