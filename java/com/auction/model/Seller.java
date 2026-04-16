package com.auction.model;

import java.util.ArrayList;
import java.util.List;

public class Seller extends User {
    private List<Item> myItems;

    public Seller(int userId, String userName, String password, String email) {
        super(userId, userName, password, email);
        this.myItems = new ArrayList<>();
    }

    public void addItem(Item item) {
        myItems.add(item);
    }
    public void editItem(Item item) {
        for (int i = 0; i < myItems.size(); i++) {
            if (myItems.get(i).getId() == item.getId()) {
                myItems.set(i, item);
                System.out.println("Item updated");
                return;
            }
        }
    }
    public void removeItem(Item item) {
        myItems.remove(item);
    }
    public List<Item> getmyItem() {
        return myItems;
    }
    @Override
    public void displayDashboard() {
        System.out.println("Seller Dashboard");
    }
}
