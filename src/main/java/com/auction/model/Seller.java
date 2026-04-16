package com.auction.model;

import java.util.ArrayList;
import java.util.List;

public class Seller extends User {
    private List<Item> myItem;

    public Seller(int userId, String userName, String password, String email) {
        super(userId, userName, password, email);
        this.myItem = new ArrayList<>();
    }

    public void addItem(Item item) {
        myItem.add(item);
    }
    public void editItem(Item item) {
        for (int i = 0; i < myItem.size(); i++) {
            if (myItem.get(i).getId() == item.getId()) {
                myItem.set(i, item);
                System.out.println("Item updated");
                return;
            }
        }
    }
    public void removeItem(Item item) {
        myItem.remove(item);
    }
    public List<Item> getmyItem() {
        return myItem;
    }
    @Override
    public void displayDashboard() {
        System.out.println("Seller Dashboard");
    }
}
