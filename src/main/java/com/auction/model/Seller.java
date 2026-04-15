package com.auction.model;

import java.util.ArrayList;
import java.util.List;

public class Seller extends User {
    private List<Item> listedItems;

    public Seller(int id, String username, String password, String email) {
        super(id, username, password, email);
        this.listedItems = new ArrayList<>();
    }

    public void addItem(Item item) {
        listedItems.add(item);
    }

    public void removeItem(Item item) {
        listedItems.remove(item);
    }

    @Override
    public void displayDashboard() {
        System.out.println("Seller Dashboard");
    }
}