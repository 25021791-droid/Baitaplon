package com.auction.common.model;

import java.util.ArrayList;
import java.util.List;

public class Seller extends User {

    private List<Item> myItems;

    public Seller(Integer userId, String name, String email) {
        super(userId, name, email, "SELLER");
        this.myItems = new ArrayList<>();
    }

    public void addItem(Item item) {
        myItems.add(item);
        System.out.println("Item added: " + item.getName());
    }

    public void editItem(Item item) {
        for (int i = 0; i < myItems.size(); i++) {
            // 🔥 ĐÃ FIX: Sử dụng .equals() thay vì == để so sánh kiểu đối tượng Integer
            if (myItems.get(i).getId().equals(item.getId())) {
                myItems.set(i, item);
                System.out.println("Item updated: " + item.getName());
                return;
            }
        }
        System.out.println("Không tìm thấy vật phẩm để cập nhật.");
    }

    public void removeItem(Item item) {
        if (myItems.remove(item)) {
            System.out.println("Item removed: " + item.getName());
        }
    }

    public List<Item> getMyItems() {
        return myItems;
    }

    @Override
    public void displayDashboard() {
        System.out.println("Seller Dashboard");
    }
}