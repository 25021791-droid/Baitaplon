package com.auction.common.model;

import java.util.ArrayList;
import java.util.List;

public class Bidder extends User {
    private double balance;
    private List<Bid> bidHistory;

    // Constructor khi đã có số dư (Khôi phục dữ liệu từ Database)
    public Bidder(int userid, String username, String email, double balance) {
        super(userid, username, email, "BIDDER");
        this.balance = balance;
        this.bidHistory = new ArrayList<>();
    }

    // Constructor khi tạo tài khoản mới (Tặng sẵn ví 100,000.0 để test)
    public Bidder(int userid, String username, String email) {
        this(userid, username, email, 100000.0);
    }

    // ================= GETTERS & SETTERS =================

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

    // ================= BUSINESS LOGIC =================

    /**
     * Thực hiện đặt giá cho một phiên đấu giá.
     * Đã bỏ tham số Item thừa thãi vì Item nằm sẵn trong Auction.
     */
    public boolean placeBid(Auction auction, double price) {
        if (price > this.balance) {
            System.out.println("[Từ chối] Số dư trong ví không đủ để thực hiện mức cược này.");
            return false;
        }
        if (price <= auction.getCurrentPrice()) {
            System.out.println("[Từ chối] Mức giá đặt phải cao hơn giá cao nhất hiện tại.");
            return false;
        }

        // 1. Tạo bản ghi lượt cược mới
        Bid newBid = new Bid(this, price);
        this.bidHistory.add(newBid);

        // 2. 🔥 ĐÃ FIX: Lưu lượt cược vào thẳng danh sách của phiên đấu giá thay vì Item
        auction.getBids().add(newBid);

        // 3. 🔥 QUAN TRỌNG: Cập nhật lại giá đỉnh (Top Price) của phiên đấu giá
        auction.setCurrentPrice(price);

        System.out.println("[Thành công] Lượt cược của " + this.getName() + " đã được ghi nhận. Giá mới: " + price);
        return true;
    }

    public void registerAutoBid(Item item, double maxBid, double increment) {
        // Tạm thời để stub (khung trống) cho tính năng tương lai
        System.out.println("Tính năng đấu giá tự động (AutoBid) đã được kích hoạt cho: " + item.getName());
    }
}