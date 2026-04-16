package com.auction.model;

public class Admin extends User {

    public Admin(int userId, String username, String password, String email) {
        super(userId, username, password, email);
    }

    public void manageUsers() {
        System.out.println("Managing users...");
    }

    public void manageAuctions() {
        System.out.println("Managing auctions...");
    }
    
    public void resolveDispute() {
        System.out.println("Resolving disputes...");
    }
 
    @Override
    public void displayDashboard() {
        System.out.println("Admin Dashboard");
    }
}
